package me.hosairis.matchvault.tracking.listeners.player

import de.marcely.bedwars.api.BedwarsAPI
import de.marcely.bedwars.api.event.player.PlayerPickupDropEvent
import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent
import de.marcely.bedwars.tools.Helper
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.storage.database.service.PlayerService
import me.hosairis.matchvault.tracking.TrackerCache
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerStatsListener(
    private val matchService: MatchService,
    private val playerService: PlayerService
): Listener {

    private val allowedStats = setOf(
        "bedwars:kills",
        "bedwars:final_kills",
        "bedwars:deaths",
        "bedwars:beds_destroyed"
    )

    @EventHandler
    private fun onStatsChange(event: PlayerStatChangeEvent) {
        if (event.isFromRemoteServer) return
        if (!event.stats.isGameStats) return
        if (event.key !in allowedStats) return

        val player = Bukkit.getPlayer(event.stats.playerUUID) ?: return
        val uuid = player.uniqueId
        val arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player) ?: return
        val matchId = TrackerCache.matchIds[arena] ?: return

        CoroutineHelper.runAsync {
            try {
                matchService.updateMatchPlayerStat(
                    matchId = matchId,
                    playerUuid = player.uniqueId,
                    statKey = event.key,
                    newValue = event.newValue.toInt()
                )
            } catch (ex: Exception) {
                Log.severe("PlayerStatChangeEvent: error for ${player.name} ($uuid) matchId=$matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    private val allowedMaterials = setOf(
        Helper.get().getMaterialByName("iron_ingot"),
        Helper.get().getMaterialByName("gold_ingot"),
        Helper.get().getMaterialByName("diamond"),
        Helper.get().getMaterialByName("emerald")
    )

    @EventHandler
    fun onResourcePickup(event: PlayerPickupDropEvent) {
        val type = event.item.itemStack.type
        if (type !in allowedMaterials) return

        val matchId = TrackerCache.matchIds[event.arena] ?: return
        val player = event.player
        val uuid = player.uniqueId
        val amount = event.item.itemStack.amount
        val fromSpawner = event.isFromSpawner

        CoroutineHelper.runAsync {
            try {
                matchService.updateResourcePickup(
                    matchId = matchId,
                    playerUuid = uuid,
                    material = type,
                    amount = amount,
                    fromSpawner = fromSpawner
                )
            } catch (ex: Throwable) {
                Log.severe("PlayerPickupDropEvent: error for ${player.name} ($uuid) matchId=$matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}