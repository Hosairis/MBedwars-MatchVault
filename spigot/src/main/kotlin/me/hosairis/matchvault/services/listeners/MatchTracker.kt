package me.hosairis.matchvault.services.listeners

import de.marcely.bedwars.api.event.arena.RoundEndEvent
import de.marcely.bedwars.api.event.arena.RoundStartEvent
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.TrackerService
import me.hosairis.matchvault.storage.database.MatchPlayers
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
                    // Create match
                    val matchData = MatchData(
                        arenaName,
                        mode,
                        timestamp
                    ).also {
                        if (!it.create()) throw IllegalStateException("RoundStartEvent: failed to create matches row for arena $arenaName (mode=$mode)")
                        it.id ?: throw NullPointerException("RoundStartEvent: matches row for arena $arenaName (mode=$mode) is missing the ID")
                    }

                    teamAssignments.forEach { (team, players) ->
                        // Create match_teams
                        val teamData = MatchTeamData(
                            matchData.id!!,
                            team.name
                        ).also {
                            if (!it.create()) throw IllegalStateException("RoundStartEvent: failed to create match_teams row for team ${team.name} in arena $arenaName (mode=$mode)")
                            it.id ?: throw NullPointerException("RoundStartEvent: match_teams row for team ${team.name} in arena $arenaName (mode=$mode) is missing the ID")
                        }

                        for ((uuid, name) in players) {
                            // Get-or-create playerId.
                            val playerId = TrackerService.playerIds.getOrPut(uuid) {
                                val player = PlayerData.read(uuid) ?: PlayerData(
                                    name = name,
                                    uuid = uuid,
                                    firstSeen = timestamp,
                                    lastSeen = timestamp
                                ).also {
                                    if (!it.create()) throw IllegalStateException("RoundStartEvent: failed to create players row for $name ($uuid)")
                                    it.id ?: throw IllegalStateException("RoundStartEvent: players row for $name ($uuid) is missing the ID")
                                }
                                player.id!!  // whatever field holds the DB id
                            }

                            // Create match player
                            val matchPlayerData = MatchPlayerData(
                                matchData.id!!,
                                playerId,
                                teamData.id!!
                            ).also {
                                if (!it.create()) throw IllegalStateException("RoundStartEvent: failed to create match_players row for $name ($uuid) on team ${team.name} in arena $arenaName (mode=$mode)")
                                it.id ?: throw IllegalStateException("RoundStartEvent: match_players row for $name ($uuid) is missing the ID")
                            }
                        }
                    }
                    TrackerService.matchIds[arena] = matchData.id!!
                }
            } catch (ex: Exception) {
                Log.severe("RoundStartEvent: error creating match for arena $arenaName (mode=$mode): ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onRoundEnd(event: RoundEndEvent) {
        val arena = event.arena
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.warning("RoundEndEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val isTie = event.isTie
        val winnerTeam = event.winnerTeam
        val winners = event.winners
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                transaction {
                    // Get all teams related to the match
                    val teamsData = MatchTeamData.readByMatchId(matchId)
                    val winnerTeamData =
                        if (isTie || winnerTeam == null) null
                        else teamsData.firstOrNull { it.id != null && it.team == winnerTeam.name }
                    val winnerTeamEntity =
                        if (winnerTeamData == null) null
                        else EntityID(winnerTeamData.id!!, MatchTeams)

                    // Update match
                    MatchData.update(matchId) { fetchRow ->
                        val row = fetchRow()
                        if (row == null) {
                            Log.severe("RoundEndEvent: match $matchId not found while ending arena ${arena.name}")
                        } else {
                            this[Matches.endedAt] = timestamp
                            this[Matches.duration] = timestamp - row[Matches.startedAt]
                            this[Matches.status] = MatchStatus.ENDED
                            this[Matches.isTie] = isTie
                            this[Matches.winnerTeamId] = winnerTeamEntity
                        }
                    }

                    // Update winners placement if there is a winner
                    if (!isTie && winnerTeamData != null) {
                        MatchTeamData.update(winnerTeamData.id!!) {
                            this[MatchTeams.finalPlacement] = 1
                        }
                    }

                    // Set final placement of every team that have their placement null to 2
                    if (isTie) {
                        teamsData.forEach { teamData ->
                            if (teamData.finalPlacement == null) {
                                MatchTeamData.update(teamData.id!!) {
                                    this[MatchTeams.finalPlacement] = 2
                                }
                            }
                        }
                    }

                    // Update winners "won" field
                    if (winners.isNotEmpty()) {
                        for (player in winners) {
                            val playerId =
                                TrackerService.playerIds[player.uniqueId]
                                    ?: PlayerData.read(uuid = player.uniqueId)?.id
                                    ?: run {
                                        Log.severe("RoundEndEvent: missing player ID for ${player.name} (${player.uniqueId})")
                                        return@transaction
                                    }
                            MatchPlayerData.update(playerId) {
                                this[MatchPlayers.won] = true
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.severe("RoundEndEvent: error closing match $matchId for arena ${arena.name}: ${ex.message}")
                ex.printStackTrace()
            } finally {
                TrackerService.matchIds.remove(arena)
            }
        }
    }
}
