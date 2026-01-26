package me.hosairis.matchvault.storage.database.cache

import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal object MatchHistoryCache {
    private val nameToMatchList = ConcurrentHashMap<String, Map<MatchData, Boolean>>()
//        Caffeine
//            .newBuilder()
//            .expireAfterWrite(Duration.ofMinutes(5))
//            .build<String, Map<MatchData, Boolean>>()

    private val matchToTeamList = ConcurrentHashMap<Long, List<MatchTeamData>>()
//        Caffeine
//            .newBuilder()
//            .expireAfterWrite(Duration.ofMinutes(10))
//            .build<Long, List<MatchTeamData>>()

    private val teamToPlayerList = ConcurrentHashMap<Long, List<MatchPlayerData>>()
//        Caffeine
//            .newBuilder()
//            .expireAfterWrite(Duration.ofMinutes(10))
//            .build<Long, List<MatchPlayerData>>()

    private val playerIdToUuid = ConcurrentHashMap<Long, UUID>()

    private val playerToTeamInMatch = ConcurrentHashMap<String, Map<Long, Long>>()

    // ---------- Inserts ---------- //

    fun putMatchList(playerName: String, matchList: Map<MatchData, Boolean>) {
        nameToMatchList.put(playerName, matchList)
    }

    fun putTeamList(matchId: Long, teamList: List<MatchTeamData>) {
        matchToTeamList.put(matchId, teamList)
    }

    fun putPlayerList(teamId: Long, playerList: List<MatchPlayerData>) {
        teamToPlayerList.put(teamId, playerList)
    }

    fun putPlayerUuid(id: Long, uuid: UUID) {
        playerIdToUuid.put(id, uuid)
    }

    fun putPlayerTeamIdInMatch(playerName: String, matchId: Long, teamId: Long) {
        val data = playerToTeamInMatch.get(playerName)?.toMutableMap() ?: return
        data.put(matchId, teamId)
        playerToTeamInMatch.put(playerName, data)
    }

    // ---------- Retrievals ---------- //

    fun getMatchList(playerName: String): Map<MatchData, Boolean>? {
        return nameToMatchList.get(playerName)
//        return nameToMatchList.getIfPresent(playerName)
    }

    fun getTeamList(matchId: Long): List<MatchTeamData>? {
        return matchToTeamList.get(matchId)
//        return matchToTeamList.getIfPresent(matchId)
    }

    fun getPlayerList(teamId: Long): List<MatchPlayerData>? {
        return teamToPlayerList.get(teamId)
//        return teamToPlayerList.getIfPresent(teamId)
    }

    fun getPlayerUuid(id: Long): UUID? {
        return playerIdToUuid.get(id)
    }

    fun getTeamIdInMatch(playerName: String, matchId: Long): Long? {
        return playerToTeamInMatch.get(playerName)?.get(matchId)
    }
}