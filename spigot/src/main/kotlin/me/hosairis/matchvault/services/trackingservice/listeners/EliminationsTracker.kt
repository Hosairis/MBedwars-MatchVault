package me.hosairis.matchvault.services.trackingservice.listeners

import de.marcely.bedwars.api.event.arena.ArenaBedBreakEvent
import de.marcely.bedwars.api.event.arena.TeamEliminateEvent
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.trackingservice.TrackerService
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.data.MatchTeamData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class EliminationsTracker: Listener {
    @EventHandler
    private fun onBedBreak(event: ArenaBedBreakEvent) {
        if (event.result == ArenaBedBreakEvent.Result.CANCEL) return

        val arena = event.arena
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.warning("BedBreakEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val teamName = event.team.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val teamData = MatchTeamData.readByMatchId(matchId)
                        .find { it.team == teamName }
                        ?.takeIf { it.id != null }
                        ?: run {
                            Log.severe("BedBreakEvent: team $teamName not found for matchId $matchId in match_teams")
                            return@transaction
                        }

                    MatchTeamData.update(teamData.id!!) {
                        this[MatchTeams.bedDestroyedAt] = timestamp
                    }
                }
            } catch (ex: Exception) {
                Log.severe("BedBreakEvent: error recording bed destruction for team $teamName in matchId $matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onTeamElimination(event: TeamEliminateEvent) {
        val arena = event.arena
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.warning("TeamEliminateEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val teamName = event.team.name
        val placement = arena.teamsWithPlayers.size + 1
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val teamData = MatchTeamData.readByMatchId(matchId)
                        .find { it.team == teamName }
                        ?.takeIf { it.id != null }
                        ?: run {
                            Log.severe("TeamEliminateEvent: team $teamName not found for matchId $matchId in match_teams")
                            return@transaction
                        }

                    MatchTeamData.update(teamData.id!!) {
                        this[MatchTeams.eliminatedAt] = timestamp
                        this[MatchTeams.finalPlacement] = placement
                    }
                }
            } catch (ex: Exception) {
                Log.severe("TeamEliminateEvent: error recording elimination for team $teamName in matchId $matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}
