package me.hosairis.matchvault.tracking.player

import de.marcely.bedwars.api.BedwarsAPI
import de.marcely.bedwars.api.arena.Arena
import de.marcely.bedwars.api.event.player.PlayerDeathInventoryDropEvent
import de.marcely.bedwars.api.event.player.PlayerPickupDropEvent
import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent
import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.MatchService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.concurrent.atomic.AtomicInteger

class PlayerStatsListener : Listener {

    @EventHandler
    private fun onStatsChange(event: PlayerStatChangeEvent) {
        if (event.isFromRemoteServer) return
        if (!event.stats.isGameStats) return
        Log.info("stat key: ${event.key} | ${event.newValue.toInt()}")
        if (event.key !in Config.values.allowedStats) return

        val player = Bukkit.getPlayer(event.stats.playerUUID) ?: return
        val uuid = player.uniqueId
        val arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player) ?: return

        CoroutineHelper.runAsync {
            try {
                MatchService.updateMatchPlayerStat(
                    arena = arena,
                    playerUuid = player.uniqueId,
                    statKey = event.key,
                    newValue = event.newValue.toInt()
                )
            } catch (ex: Exception) {
                Log.severe("PlayerStatChangeEvent: error for ${player.name} ($uuid) matchId=${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onResourcePickup(event: PlayerPickupDropEvent) {
        val type = event.item.itemStack.type
        if (type !in Config.values.allowedMaterials) return

        val player = event.player
        val arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player) ?: return
        val uuid = player.uniqueId
        val amount = event.item.itemStack.amount
        val fromSpawner = event.isFromSpawner

        CoroutineHelper.runAsync {
            try {
                MatchService.updateResourcePickup(
                    arena = arena,
                    playerUuid = uuid,
                    material = type,
                    amount = amount.toLong(),
                    fromSpawner = fromSpawner
                )
            } catch (ex: Throwable) {
                Log.severe("PlayerPickupDropEvent: error for ${player.name} ($uuid) matchId=${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onInvDrop(event: PlayerDeathInventoryDropEvent) {
        if (event.handlerQueue.indexOf(PlayerDeathInventoryDropEvent.Handler.DEFAULT_AUTO_PICKUP) == -1) return
        val keepSpawnersIndex = event.handlerQueue.indexOf(PlayerDeathInventoryDropEvent.Handler.DEFAULT_KEEP_SPAWNERS)

        event.handlerQueue.add(
            keepSpawnersIndex + 1,
            object : PlayerDeathInventoryDropEvent.Handler {
                override fun getPlugin(): Plugin = MatchVault.instance

                override fun execute(
                    player: Player,
                    arena: Arena,
                    killer: Player?,
                    droppedItems: List<ItemStack>,
                    droppedExp: AtomicInteger
                ) {
                    val uuid = killer?.uniqueId ?: return
                    var iron = 0
                    var gold = 0
                    var diamond = 0
                    var emerald = 0

                    for (item in droppedItems) {
                        if (item.type == Config.values.allowedMaterials[0]) {
                            iron += item.amount
                        } else if (item.type == Config.values.allowedMaterials[1]) {
                            gold += item.amount
                        } else if (item.type == Config.values.allowedMaterials[2]) {
                            diamond += item.amount
                        } else if (item.type == Config.values.allowedMaterials[3]) {
                            emerald += item.amount
                        }
                    }
                    Log.info("(sync) iron: $iron | gold: $gold | diamond: $diamond | emerald: $emerald")

//                    val itemsSnapshot: Map<Material, Long> =
//                        droppedItems.fold(mutableMapOf<Material, Long>()) { acc, item ->
//                            val amt = item.amount
//                            if (amt > 0) acc[item.type] = (acc[item.type] ?: 0L) + amt.toLong()
//                            acc
//                        }.toMap()

                    CoroutineHelper.runAsync {
                        try {
                            Log.info("(Async) iron: $iron | gold: $gold | diamond: $diamond | emerald: $emerald")
                            MatchService.updateResourcePickup(
                                arena = arena,
                                playerUuid = uuid,
                                material = Config.values.allowedMaterials[0],
                                amount = iron.toLong(),
                                fromSpawner = false
                            )
                            MatchService.updateResourcePickup(
                                arena = arena,
                                playerUuid = uuid,
                                material = Config.values.allowedMaterials[1],
                                amount = gold.toLong(),
                                fromSpawner = false
                            )
                            MatchService.updateResourcePickup(
                                arena = arena,
                                playerUuid = uuid,
                                material = Config.values.allowedMaterials[2],
                                amount = diamond.toLong(),
                                fromSpawner = false
                            )
                            MatchService.updateResourcePickup(
                                arena = arena,
                                playerUuid = uuid,
                                material = Config.values.allowedMaterials[3],
                                amount = emerald.toLong(),
                                fromSpawner = false
                            )

//                            Log.info("snapshot async = $itemsSnapshot") // add this once to prove it
//                            for ((material, amount) in itemsSnapshot) {
//                                MatchService.updateResourcePickup(
//                                    arena = arena,
//                                    playerUuid = uuid,
//                                    material = material,
//                                    amount = amount,
//                                    fromSpawner = false
//                                )
//                            }
                        } catch (ex: Throwable) {
                            Log.severe("DropEvent async error uuid=$uuid arenaId=${arena.name}: ${ex.message}")
                            ex.printStackTrace()
                        }
                    }
                }
            }
        )
    }
}