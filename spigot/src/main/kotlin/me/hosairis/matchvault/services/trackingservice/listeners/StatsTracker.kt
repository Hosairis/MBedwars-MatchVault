package me.hosairis.matchvault.services.trackingservice.listeners


import de.marcely.bedwars.api.BedwarsAPI
import de.marcely.bedwars.api.event.player.PlayerPickupDropEvent
import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.trackingservice.TrackerService
import me.hosairis.matchvault.storage.database.MatchPlayers
import me.hosairis.matchvault.storage.database.data.MatchPlayerData
import me.hosairis.matchvault.storage.database.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class StatsTracker: Listener {
    private val allowedStats = listOf(
        "bedwars:kills",
        "bedwars:final_kills",
        "bedwars:deaths",
        "bedwars:beds_destroyed")

    @EventHandler
    private fun onStatsChange(event: PlayerStatChangeEvent) {
        if (event.isFromRemoteServer) return
        if (!event.stats.isGameStats) return
        if (!allowedStats.contains(event.key)) return

        val player = Bukkit.getPlayer(event.stats.playerUUID)
        if (player == null || !player.isOnline) {
            Log.warning("PlayerStatChangeEvent: player ${event.stats.playerUUID} is not online")
            return
        }
        val arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player) ?: run {
            Log.warning("PlayerStatChangeEvent: arena not found for player ${player.name}")
            return
        }
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.warning("PlayerStatChangeEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val statKey = event.key
        val newValue = event.newValue

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val playerId =
                        TrackerService.playerIds[player.uniqueId]
                            ?: PlayerData.read(uuid = player.uniqueId)?.id
                            ?: run {
                                Log.severe("PlayerStatChangeEvent: missing player ID for ${player.name} (${player.uniqueId})")
                                return@transaction
                            }
                    val playersData = MatchPlayerData.readByMatchId(matchId).find { it.playerId == playerId }
                        ?: run {
                            Log.severe("PlayerStatChangeEvent: missing match player row for ${player.name} (${player.uniqueId}) in matchId $matchId")
                            return@transaction
                        }

                    MatchPlayerData.update(playersData.id!!) {
                        when (statKey) {
                            "bedwars:kills" -> {
                                this[MatchPlayers.kills] = newValue.toInt()
                            }
                            "bedwars:final_kills" -> {
                                this[MatchPlayers.finalKills] = newValue.toInt()
                            }
                            "bedwars:deaths" -> {
                                this[MatchPlayers.deaths] = newValue.toInt()
                            }
                            "bedwars:beds_destroyed" -> {
                                this[MatchPlayers.bedsDestroyed] = newValue.toInt()
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.severe("PlayerStatChangeEvent: error updating stats for ${player.name} (${player.uniqueId}) in matchId $matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    private val allowedMaterials = listOf(
        Material.IRON_INGOT,
        Material.GOLD_INGOT,
        Material.DIAMOND,
        Material.EMERALD)
    @EventHandler
    private fun onResourcePickup(event: PlayerPickupDropEvent) {
        if (!allowedMaterials.contains(event.item.itemStack.type)) return

        val arena = event.arena
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.warning("PlayerPickupDropEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val player = event.player
        val isFromSpawner = event.isFromSpawner
        val item = event.item.itemStack.type
        val amount = event.item.itemStack.amount

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val playerId =
                        TrackerService.playerIds[player.uniqueId]
                            ?: PlayerData.read(uuid = player.uniqueId)?.id
                            ?: run {
                                Log.severe("PlayerPickupDropEvent: missing player ID for ${player.name} (${player.uniqueId})")
                                return@transaction
                            }
                    val playersData = MatchPlayerData.readByMatchId(matchId).find { it.playerId == playerId }
                        ?: run {
                            Log.severe("PlayerPickupDropEvent: missing match player row for ${player.name} (${player.uniqueId}) in matchId $matchId")
                            return@transaction
                        }

                    MatchPlayerData.update(playersData.id!!) { fetchRow ->
                        val row = fetchRow()
                        if (row == null) {
                            Log.severe("PlayerPickupDropEvent: failed to fetch match player row for ${player.name} (${player.uniqueId}) in matchId $matchId")
                            return@update
                        }

                        when (item) {
                            Material.IRON_INGOT -> {
                                this[MatchPlayers.resIron] = row[MatchPlayers.resIron] + amount
                                if (isFromSpawner) this[MatchPlayers.resIronSpawner] = row[MatchPlayers.resIronSpawner] + amount
                            }
                            Material.GOLD_INGOT -> {
                                this[MatchPlayers.resGold] = row[MatchPlayers.resGold] + amount
                                if (isFromSpawner) this[MatchPlayers.resGoldSpawner] = row[MatchPlayers.resGoldSpawner] + amount
                            }
                            Material.DIAMOND -> {
                                this[MatchPlayers.resDiamond] = row[MatchPlayers.resDiamond] + amount
                                if (isFromSpawner) this[MatchPlayers.resDiamondSpawner] = row[MatchPlayers.resDiamondSpawner] + amount
                            }
                            Material.EMERALD -> {
                                this[MatchPlayers.resEmerald] = row[MatchPlayers.resEmerald] + amount
                                if (isFromSpawner) this[MatchPlayers.resEmeraldSpawner] = row[MatchPlayers.resEmeraldSpawner] + amount
                            }
                            else -> {}
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.severe("PlayerPickupDropEvent: error updating resource pickup for ${player.name} (${player.uniqueId}) in matchId $matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}
