package me.hosairis.matchvault.tracking.listeners.match

import de.marcely.bedwars.api.event.arena.RoundEndEvent
import de.marcely.bedwars.api.event.arena.RoundStartEvent
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.tracking.TrackerCache
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MatchRoundListener(
    private val matchService: MatchService
): Listener {

    @EventHandler
    private fun onRoundStart(event: RoundStartEvent) {
        val arena = event.arena
        val arenaName = arena.name
        val mode = arena.playersPerTeam
        val timestamp = System.currentTimeMillis()
        val teamOrder = arena.teamsWithPlayers.associateWith { team ->
            arena.getPlayersInTeam(team).map { it.uniqueId }
        }

        CoroutineHelper.runAsync {
            try {
                val matchId = matchService.startMatch(
                    arenaName = arenaName,
                    mode = mode,
                    startedAt = timestamp,
                    teamMap = teamOrder
                )
                TrackerCache.matchIds[arena] = matchId
            } catch (ex: Exception) {
                Log.severe("RoundStartEvent: error creating match for arena ${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onRoundEnd(event: RoundEndEvent) {
        val arena = event.arena
        val matchId = TrackerCache.matchIds[arena] ?: run {
            Log.warning("RoundEndEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }

        val endedAt = System.currentTimeMillis()
        val isTie = event.isTie
        val winnerTeamName = event.winnerTeam?.name
        val winnersUuids = event.winners.map { it.uniqueId }

        CoroutineHelper.runAsync {
            try {
                matchService.endMatch(
                    matchId = matchId,
                    endedAt = endedAt,
                    isTie = isTie,
                    winnerTeamName = winnerTeamName,
                    winnersUuids = winnersUuids
                )
            } catch (ex: Exception) {
                Log.severe("RoundEndEvent: error ending match $matchId for arena ${arena.name}: ${ex.message}")
                ex.printStackTrace()
            } finally {
                TrackerCache.matchIds.remove(arena)
            }
        }
    }
}