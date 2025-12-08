package me.hosairis.matchvault.services.listeners

import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.TrackerService
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.data.PlayerData
import org.bukkit.Bukkit
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

        handlePlayer(playerName, playerUuid, timestamp, Type.ENTER)
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val playerName = player.name
        val playerUuid = player.uniqueId
        val timestamp = System.currentTimeMillis()

        handlePlayer(playerName, playerUuid, timestamp, Type.EXIT)
    }

    private fun handlePlayer(
        name: String,
        uuid: UUID,
        timestamp: Long,
        type: Type
    ) {
        CoroutineHelper.runAsync {
            try {
                transaction {
                    // Reade the player
                    var playerData = PlayerData.read(uuid)
                    // Create if it does not exist
                    if (playerData == null) {
                        playerData = PlayerData(
                            name = name,
                            uuid = uuid,
                            firstSeen = timestamp,
                            lastSeen = timestamp
                        ).also {
                            if (!it.create()) throw IllegalStateException("Failed to create row in table (players) for player ($name | $uuid)")
                        }
                    } else {
                        // Update if it exists
                        PlayerData.update(uuid) {
                            this[Players.name] = name
                            this[Players.lastSeen] = timestamp
                        }
                        playerData.name = name
                        playerData.lastSeen = timestamp
                    }
                    playerData.id ?: throw IllegalStateException("Data in table (players) for player ($name | $uuid) is missing the ID")

                    // Store or clear player id cache
                    if (type == Type.ENTER) {
                        TrackerService.playerIds[uuid] = playerData.id!!
                    } else {
                        TrackerService.playerIds.remove(uuid)
                    }
                }
            } catch (ex: Exception) {
                if (type == Type.ENTER) {
                    Log.warning("An error occurred while handling join event: ${ex.message}")
                } else {
                    Log.warning("An error occurred while handling quit event: ${ex.message}")
                }
                ex.printStackTrace()

                //TODO: Make this configurable
                CoroutineHelper.runSync {
                    Bukkit.getPlayer(uuid)?.kickPlayer("Database error while logging you in. Please try again.")
                }
            }
        }
    }

    private enum class Type {
        EXIT,
        ENTER
    }
}
