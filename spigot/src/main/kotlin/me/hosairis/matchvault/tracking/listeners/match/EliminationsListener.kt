package me.hosairis.matchvault.tracking.listeners.match

import de.marcely.bedwars.api.event.arena.ArenaBedBreakEvent
import de.marcely.bedwars.api.event.arena.TeamEliminateEvent
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.tracking.TrackerCache
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EliminationsListener(
    private val matchService: MatchService
): Listener {

    @EventHandler
    fun onBedBreak(event: ArenaBedBreakEvent) {
        if (event.result == ArenaBedBreakEvent.Result.CANCEL) return

        val arena = event.arena
        val matchId = TrackerCache.matchIds[arena] ?: run {
            Log.warning("BedBreakEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }

        val teamName = event.team.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                matchService.recordBedBreak(
                    matchId = matchId,
                    teamName = teamName,
                    timestamp = timestamp
                )
            } catch (t: Throwable) {
                Log.severe("BedBreakEvent: error recording bed break for team $teamName matchId=$matchId: ${t.message}")
                t.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onTeamElimination(event: TeamEliminateEvent) {
        val arena = event.arena
        val matchId = TrackerCache.matchIds[arena] ?: run {
            Log.warning("TeamEliminateEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }

        val teamName = event.team.name
        val placement = arena.teamsWithPlayers.size + 1
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                matchService.recordTeamElimination(
                    matchId = matchId,
                    teamName = teamName,
                    placement = placement,
                    timestamp = timestamp
                )
            } catch (t: Throwable) {
                Log.severe("TeamEliminateEvent: error recording elimination for team $teamName matchId=$matchId: ${t.message}")
                t.printStackTrace()
            }
        }
    }
}