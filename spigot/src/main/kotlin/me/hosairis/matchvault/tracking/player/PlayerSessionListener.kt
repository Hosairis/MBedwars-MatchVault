package me.hosairis.matchvault.tracking.player

import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.PlayerService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerSessionListener: Listener {

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val name = player.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                PlayerService.upsertSeen(
                    uuid = uuid,
                    name = name,
                    seenAt = timestamp,
                    cache = true
                )
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
                PlayerService.upsertSeen(
                    uuid = uuid,
                    name = name,
                    seenAt = timestamp,
                    cache = false
                )
            } catch (ex: Throwable) {
                Log.severe("PlayerQuitEvent: error processing player $name ($uuid): ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}