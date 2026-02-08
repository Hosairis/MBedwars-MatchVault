package me.hosairis.matchvault.tracking.match

import de.marcely.bedwars.api.event.arena.RoundEndEvent
import de.marcely.bedwars.api.event.arena.RoundStartEvent
import de.marcely.bedwars.api.player.DefaultPlayerStatSet
import de.marcely.bedwars.api.player.PlayerDataAPI
import me.hosairis.matchvault.storage.database.cache.MatchHistoryCache
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.MatchService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MatchRoundListener: Listener {

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
                MatchService.startMatch(
                    arena = arena,
                    arenaName = arenaName,
                    mode = mode,
                    startedAt = timestamp,
                    teamMap = teamOrder
                )
            } catch (ex: Exception) {
                Log.severe("RoundStartEvent: error creating match for arena ${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }

        arena.players.forEach { MatchHistoryCache.removeMatchList(it.name) }
    }

    @EventHandler
    private fun onRoundEnd(event: RoundEndEvent) {
        val arena = event.arena
        val endedAt = System.currentTimeMillis()
        val isTie = event.isTie
        val winnerTeamName = event.winnerTeam?.name
        val winnersUuids = event.winners.map { it.uniqueId } + event.quitWinners.map { it.uniqueId }
        val allPlayers =
            event.winners.map { it.name to it.uniqueId } +
            event.quitWinners.map { it.username to it.uniqueId } +
            event.losers.map { it.name to it.uniqueId } +
            event.quitLosers.map { it.username to it.uniqueId }

        allPlayers.forEach { (_, uuid) ->
            PlayerDataAPI.get().getStats(uuid) {
                DefaultPlayerStatSet.PLAY_TIME.getDisplayedValue(it)
            }
        }

        CoroutineHelper.runAsync {
            try {
                MatchService.endMatch(
                    arena = arena,
                    endedAt = endedAt,
                    isTie = isTie,
                    winnerTeamName = winnerTeamName,
                    winnersUuids = winnersUuids
                )
            } catch (ex: Exception) {
                Log.severe("RoundEndEvent: error ending for arena ${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }

        allPlayers.forEach { (name, _) ->
            MatchHistoryCache.removeMatchList(name)
        }
    }
}