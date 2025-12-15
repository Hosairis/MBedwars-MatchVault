package me.hosairis.matchvault.tracking.listeners.player

import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.PlayerService
import me.hosairis.matchvault.tracking.TrackerCache
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerSessionListener(
    private val playerService: PlayerService
): Listener {

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val name = player.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                val playerData = playerService.upsertSeen(uuid = uuid, name = name, seenAt = timestamp)
                val id = playerData.id ?: run {
                    Log.severe("PlayerJoinEvent: upsertSeen returned player with null id for $name ($uuid)")
                    return@runAsync
                }
                TrackerCache.playerIds[uuid] = id
            } catch (ex: Exception) {
                Log.severe("PlayerJoinEvent: error processing player $name ($uuid): ${ex.message}")
                ex.printStackTrace()

                //TODO: make configurable
                CoroutineHelper.runSync {
                    Bukkit.getPlayer(uuid)?.kickPlayer("Database error while logging you in. Please try again.")
                }
            }
        }
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val name = player.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                playerService.upsertSeen(uuid = uuid, name = name, seenAt = timestamp)
            } catch (ex: Throwable) {
                Log.severe("PlayerQuitEvent: error processing player $name ($uuid): ${ex.message}")
                ex.printStackTrace()
            } finally {
                TrackerCache.playerIds.remove(uuid)
            }
        }
    }
}