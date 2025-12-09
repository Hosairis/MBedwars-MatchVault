package me.hosairis.matchvault.services.listeners

import de.marcely.bedwars.api.event.arena.ArenaBedBreakEvent
import de.marcely.bedwars.api.event.arena.TeamEliminateEvent
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.TrackerService
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
            Log.severe("Failed to obtain id to match (${arena.name} | ${arena.maxPlayers})")
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
                            Log.severe("Failed to obtain team (${teamName}) from table (match_teams) from match ($matchId)")
                            return@transaction
                        }

                    MatchTeamData.update(teamData.id!!) {
                        this[MatchTeams.bedDestroyedAt] = timestamp
                    }
                }
            } catch (ex: Exception) {
                Log.warning("An error occurred while handling bed destroyed event: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onTeamElimination(event: TeamEliminateEvent) {
        val arena = event.arena
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.severe("Failed to obtain id to match (${arena.name} | ${arena.maxPlayers})")
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
                            Log.severe("Failed to obtain team (${teamName}) from table (match_teams) from match ($matchId)")
                            return@transaction
                        }

                    MatchTeamData.update(teamData.id!!) {
                        this[MatchTeams.eliminatedAt] = timestamp
                        this[MatchTeams.finalPlacement] = placement
                    }
                }
            } catch (ex: Exception) {
                Log.warning("An error occurred while handling team eliminated event: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}