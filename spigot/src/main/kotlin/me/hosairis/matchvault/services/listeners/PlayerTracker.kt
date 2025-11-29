package me.hosairis.matchvault.services.listeners

import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class PlayerTracker : Listener {
    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerName = player.name
        val playerUuid = player.uniqueId
        val timestamp = System.currentTimeMillis()

        handlePlayer(playerName, playerUuid, timestamp)
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val playerName = player.name
        val playerUuid = player.uniqueId
        val timestamp = System.currentTimeMillis()

        handlePlayer(playerName, playerUuid, timestamp)
    }

    private fun handlePlayer(
        name: String,
        uuid: UUID,
        timestamp: Long
    ) {
        CoroutineHelper.runAsync {
            try {
                transaction {
                    var playerData = PlayerData.read(uuid)
                    if (playerData == null) {
                        playerData = PlayerData(
                            name = name,
                            uuid = uuid,
                            firstSeen = timestamp,
                            lastSeen = timestamp
                        ).also {
                            if (!it.create()) throw IllegalStateException("Failed to create player row for $name ($uuid)")
                        }
                    } else {
                        PlayerData.update(uuid) {
                            this[Players.name] = name
                            this[Players.lastSeen] = timestamp
                        }
                        playerData.name = name
                        playerData.lastSeen = timestamp
                    }
                    playerData.id ?: throw IllegalStateException("Missing database id for $name ($uuid)")
                }
            } catch (ex: Exception) {
                Log.warning("Error handling join for $name ($uuid): ${ex.message}")
                CoroutineHelper.runSync {
                    Bukkit.getPlayer(uuid)?.kickPlayer("Database error while logging you in. Please try again.")
                }
            }
        }
    }
}
