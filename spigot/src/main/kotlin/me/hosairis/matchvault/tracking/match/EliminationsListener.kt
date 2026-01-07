package me.hosairis.matchvault.tracking.match

import de.marcely.bedwars.api.event.arena.ArenaBedBreakEvent
import de.marcely.bedwars.api.event.arena.TeamEliminateEvent
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.MatchService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EliminationsListener: Listener {

    @EventHandler
    fun onBedBreak(event: ArenaBedBreakEvent) {
        if (event.result == ArenaBedBreakEvent.Result.CANCEL) return

        val arena = event.arena
        val teamName = event.team.name
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                MatchService.recordBedBreak(
                    arena = arena,
                    teamName = teamName,
                    timestamp = timestamp
                )
            } catch (t: Throwable) {
                Log.severe("BedBreakEvent: error recording bed break for team $teamName matchId=${arena.name}: ${t.message}")
                t.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onTeamElimination(event: TeamEliminateEvent) {
        val arena = event.arena
        val teamName = event.team.name
        val placement = arena.teamsWithPlayers.size + 1
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                MatchService.recordTeamElimination(
                    arena = arena,
                    teamName = teamName,
                    placement = placement,
                    timestamp = timestamp
                )
            } catch (t: Throwable) {
                Log.severe("TeamEliminateEvent: error recording elimination for team $teamName matchId=${arena.name}: ${t.message}")
                t.printStackTrace()
            }
        }
    }
}