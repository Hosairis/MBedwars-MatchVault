package me.hosairis.matchvault.storage.database

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseQueue {
    // Insert queues
    private val matchTeamInserts = mutableListOf<Transaction.() -> Unit>()
    private val matchPlayerInserts = mutableListOf<Transaction.() -> Unit>()
    private val shopPurchaseInserts = mutableListOf<Transaction.() -> Unit>()
    private val upgradePurchaseInserts = mutableListOf<Transaction.() -> Unit>()
    private val timelineInserts = mutableListOf<Transaction.() -> Unit>()
    private val timelineMetaInserts = mutableListOf<Transaction.() -> Unit>()
    private val genericInserts = mutableListOf<Transaction.() -> Unit>()
    // Update queue
    private val updateActions = mutableListOf<Transaction.() -> Unit>()

    private val lock = Any()

    fun queueInsert(table: Table? = null, action: Transaction.() -> Unit) {
        synchronized(lock) {
            when (table) {
                MatchTeams -> matchTeamInserts += action
                MatchPlayers -> matchPlayerInserts += action
                ShopPurchases -> shopPurchaseInserts += action
                UpgradePurchases -> upgradePurchaseInserts += action
                Timelines -> timelineInserts += action
                TimelineMetas -> timelineMetaInserts += action
                else -> genericInserts += action
            }
        }
    }

    fun queueUpdate(action: Transaction.() -> Unit) {
        synchronized(lock) { updateActions += action }
    }

    fun flush() {
        // Snapshot under lock
        val snapMatchTeams: List<Transaction.() -> Unit>
        val snapMatchPlayers: List<Transaction.() -> Unit>
        val snapShopPurchases: List<Transaction.() -> Unit>
        val snapUpgradePurchases: List<Transaction.() -> Unit>
        val snapPlayerStats: List<Transaction.() -> Unit>
        val snapTimelines: List<Transaction.() -> Unit>
        val snapTimelineMetas: List<Transaction.() -> Unit>
        val snapGeneric: List<Transaction.() -> Unit>
        val snapUpdates: List<Transaction.() -> Unit>

        synchronized(lock) {
            snapMatchTeams = matchTeamInserts.toList().also { matchTeamInserts.clear() }
            snapMatchPlayers = matchPlayerInserts.toList().also { matchPlayerInserts.clear() }
            snapShopPurchases = shopPurchaseInserts.toList().also { shopPurchaseInserts.clear() }
            snapUpgradePurchases = upgradePurchaseInserts.toList().also { upgradePurchaseInserts.clear() }
            snapTimelines = timelineInserts.toList().also { timelineInserts.clear() }
            snapTimelineMetas = timelineMetaInserts.toList().also { timelineMetaInserts.clear() }
            snapGeneric = genericInserts.toList().also { genericInserts.clear() }
            snapUpdates = updateActions.toList().also { updateActions.clear() }
        }

        if (
            snapMatchTeams.isEmpty() && snapMatchPlayers.isEmpty() &&
            snapShopPurchases.isEmpty() && snapUpgradePurchases.isEmpty() &&
            snapTimelineMetas.isEmpty() && snapGeneric.isEmpty() &&
            snapUpdates.isEmpty()
        ) return

        try {
            transaction {
                // Batch inserts for all batchable tables
                if (snapMatchTeams.isNotEmpty())
                    MatchTeams.batchInsert(snapMatchTeams) { it(this@transaction) }

                if (snapMatchPlayers.isNotEmpty())
                    MatchPlayers.batchInsert(snapMatchPlayers) { it(this@transaction) }

                if (snapShopPurchases.isNotEmpty())
                    ShopPurchases.batchInsert(snapShopPurchases) { it(this@transaction) }

                if (snapUpgradePurchases.isNotEmpty())
                    UpgradePurchases.batchInsert(snapUpgradePurchases) { it(this@transaction) }

                if (snapTimelines.isNotEmpty())
                    Timelines.batchInsert(snapTimelines) { it(this@transaction) }

                if (snapTimelineMetas.isNotEmpty())
                    TimelineMetas.batchInsert(snapTimelineMetas) { it(this@transaction) }

                // Generic single inserts
                snapGeneric.forEach { it(this) }

                // Updates last
                snapUpdates.forEach { it(this) }
            }
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Database flush failed: ${e.message}")
            e.printStackTrace()

            // Requeue failed data
            synchronized(lock) {
                matchTeamInserts += snapMatchTeams
                matchPlayerInserts += snapMatchPlayers
                shopPurchaseInserts += snapShopPurchases
                upgradePurchaseInserts += snapUpgradePurchases
                timelineInserts += snapTimelines
                timelineMetaInserts += snapTimelineMetas
                genericInserts += snapGeneric
                updateActions += snapUpdates
            }
        }
    }

    fun scheduleFlush(plugin: Plugin, intervalTicks: Long = 20L * 5) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, { flush() }, intervalTicks, intervalTicks)
    }
}
