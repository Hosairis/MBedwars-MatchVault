package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.UpgradePurchases
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

data class UpgradePurchaseData(
    val matchId: Long,
    val buyerId: Long,
    val teamId: Long,
    var upgrade: String,
    var level: Int = 1
) {
    var id: Long? = null
        private set

    companion object {
        suspend fun read(id: Long): UpgradePurchaseData? = withContext(Dispatchers.IO) {
            transaction {
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByMatchId(matchId: Long): List<UpgradePurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val matchRef = EntityID(matchId, Matches)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.matchId eq matchRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByPlayerId(buyerId: Long): List<UpgradePurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val buyerRef = EntityID(buyerId, Players)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.buyerId eq buyerRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByTeamId(teamId: Long): List<UpgradePurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val teamRef = EntityID(teamId, MatchTeams)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.teamId eq teamRef }
                    .map { it.toData() }
            }
        }

        private fun ResultRow.toData(): UpgradePurchaseData =
            UpgradePurchaseData(
                matchId = this[UpgradePurchases.matchId].value,
                buyerId = this[UpgradePurchases.buyerId].value,
                teamId = this[UpgradePurchases.teamId].value,
                upgrade = this[UpgradePurchases.upgrade],
                level = this[UpgradePurchases.level]
            ).apply {
                id = this@toData[UpgradePurchases.id].value
            }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                val newId = UpgradePurchases.insertAndGetId { statement ->
                    statement[UpgradePurchases.matchId] = EntityID(this@UpgradePurchaseData.matchId, Matches)
                    statement[UpgradePurchases.buyerId] = EntityID(this@UpgradePurchaseData.buyerId, Players)
                    statement[UpgradePurchases.teamId] = EntityID(this@UpgradePurchaseData.teamId, MatchTeams)
                    statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
                    statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
                }
                this@UpgradePurchaseData.id = newId.value
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            try {
                UpgradePurchases.update({ UpgradePurchases.id eq recordId }) { statement ->
                    statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
                    statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
                } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            try {
                UpgradePurchases.deleteWhere { UpgradePurchases.id eq recordId } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }
}
