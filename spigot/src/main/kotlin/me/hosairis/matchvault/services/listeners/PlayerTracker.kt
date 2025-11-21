package me.hosairis.matchvault.services.listeners

import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.data.PlayerData
import me.hosairis.matchvault.storage.database.data.PlayerStatsData
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class PlayerTracker : Listener {
    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerName = player.name
        val playerUuid = player.uniqueId
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                transaction {
                    var playerData = PlayerData.read(playerUuid)
                    if (playerData == null) {
                        playerData = PlayerData(
                            name = playerName,
                            uuid = playerUuid,
                            firstSeen = timestamp,
                            lastSeen = timestamp
                        ).also {
                            if (!it.create()) throw IllegalStateException("Failed to create player row for $playerName ($playerUuid)")
                        }
                    } else {
                        PlayerData.update(playerUuid) {
                            this[Players.name] = playerName
                            this[Players.lastSeen] = timestamp
                        }
                        playerData.name = playerName
                        playerData.lastSeen = timestamp
                    }
                    val playerId = playerData.id ?: throw IllegalStateException("Missing database id for $playerName ($playerUuid)")

                    val stats = PlayerStatsData.readByPlayerId(playerId)
                    if (stats == null) {
                        if (!PlayerStatsData(playerId = playerId).create()) {
                            throw IllegalStateException("Failed to create stats row for $playerName ($playerUuid)")
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.warning("Error handling join for $playerName ($playerUuid): ${ex.message}")
                CoroutineHelper.runSync {
                    Bukkit.getPlayer(playerUuid)?.kickPlayer("Database error while logging you in. Please try again.")
                }
            }
        }
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val playerUuid = player.uniqueId
        val playerName = player.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val existing = PlayerData.read(playerUuid)
                    if (existing == null) {
                        PlayerData(
                            name = playerName,
                            uuid = playerUuid,
                            firstSeen = timestamp,
                            lastSeen = timestamp
                        ).create()
                    } else {
                        PlayerData.update(playerUuid) {
                            this[Players.lastSeen] = timestamp
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.warning("Error handling quit for $playerName ($playerUuid): ${ex.message}")
            }
        }
    }
}
