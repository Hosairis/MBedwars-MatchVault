package me.hosairis.matchvault.services.listeners

import de.marcely.bedwars.api.event.arena.RoundEndEvent
import de.marcely.bedwars.api.event.arena.RoundStartEvent
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.TrackerService
import me.hosairis.matchvault.storage.database.MatchStatus
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.data.MatchData
import me.hosairis.matchvault.storage.database.data.MatchPlayerData
import me.hosairis.matchvault.storage.database.data.MatchTeamData
import me.hosairis.matchvault.storage.database.data.PlayerData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

//TODO: Handle state change for abandon
class MatchTracker : Listener {
    @EventHandler
    private fun onRoundStart(event: RoundStartEvent) {
        val arena = event.arena
        val arenaName = arena.name
        val mode = arena.playersPerTeam
        val timestamp = System.currentTimeMillis()
        val teamAssignments = arena.teamsWithPlayers.associateWith { team ->
            arena.getPlayersInTeam(team).map { it.uniqueId to it.name }
        }

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val matchData = MatchData(
                        arenaName,
                        mode,
                        timestamp
                    ).also {
                        if (!it.create()) throw IllegalStateException("Failed to create match row for $arenaName ($mode)")
                    }
                    if (matchData.id == null) throw NullPointerException("Failed to fetch Id for match $arenaName ($mode)")

                    teamAssignments.forEach { (team, players) ->
                        val teamData = MatchTeamData(
                            matchData.id!!,
                            team.name
                        ).also {
                            if (!it.create()) throw IllegalStateException("Failed to create team row for ${team.nameAsMessage} ($arenaName | $mode)")
                        }

                        for ((uuid, name) in players) {
                            val player = PlayerData.read(uuid) ?: PlayerData(
                                name = name,
                                uuid = uuid,
                                firstSeen = timestamp,
                                lastSeen = timestamp
                            ).also {
                                if (!it.create()) throw IllegalStateException("Failed to create player row for $name ($uuid)")
                            }

                            val teamData = MatchPlayerData(
                                matchData.id!!,
                                player.id!!,
                                teamData.id!!
                            ).also {
                                if (!it.create()) throw IllegalStateException("Failed to create team row for ${player.name} ($arenaName | ${team.name})")
                            }
                        }
                    }
                    TrackerService.matchIds[arena] = matchData.id!!
                }
            } catch (ex: Exception) {
                Log.warning("Failed to create match row, skipping...")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onRoundEnd(event: RoundEndEvent) {
        val arena = event.arena
        val isTie = event.isTie
        val winnerTeam = event.winnerTeam
        val winners = event.winners
        val losers = event.losers
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val matchId = TrackerService.matchIds[arena] ?: run {
                        Log.warning("match not found skipping")
                        return@transaction
                    }
                    val teamsData = MatchTeamData.readByMatchId(matchId)
                    val playersData = MatchPlayerData.readByMatchId(matchId)
                    val winnerTeamData =
                        if (isTie || winnerTeam == null) null
                        else teamsData.firstOrNull { it.id != null && it.team == winnerTeam.name }
                    val winnerTeamEntity =
                        if (winnerTeamData == null) null
                        else EntityID(winnerTeamData.id!!, MatchTeams)

                    MatchData.update(matchId) { fetchRow ->
                        val row = fetchRow()
                        if (row == null) {
                            Log.warning("failed to fetch match row")
                        } else {
                            this[Matches.endedAt] = timestamp
                            this[Matches.duration] = timestamp - row[Matches.startedAt]
                            this[Matches.status] = MatchStatus.ENDED
                            this[Matches.isTie] = isTie
                            this[Matches.winnerTeamId] = winnerTeamEntity
                        }
                    }

                    if (!isTie && winnerTeamData != null) {
                        MatchTeamData.update(winnerTeamData.id!!) {
                            this[MatchTeams.finalPlacement] = 1
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.warning("Failed to handle match end, skipping...")
                ex.printStackTrace()
            } finally {
                TrackerService.matchIds.remove(arena)
            }
        }
    }
}
