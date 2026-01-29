package me.hosairis.matchvault.storage.database.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import java.util.UUID
import java.util.concurrent.TimeUnit

internal object MatchHistoryCache {

    private val nameToMatchList: Cache<String, Map<MatchData, Boolean>> =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()

    private val matchToTeamList: Cache<Long, List<MatchTeamData>> =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()

    private val teamToPlayerList: Cache<Long, List<MatchPlayerData>> =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()

    private val playerIdToUuid: Cache<Long, UUID> =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()

    private val playerToTeamInMatch: Cache<String, Long> =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()

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
        playerToTeamInMatch.put("$playerName|$matchId", teamId)
    }

    // ---------- Retrievals ---------- //

    fun getMatchList(playerName: String): Map<MatchData, Boolean>? =
        nameToMatchList.getIfPresent(playerName)

    fun getTeamList(matchId: Long): List<MatchTeamData>? =
        matchToTeamList.getIfPresent(matchId)

    fun getPlayerList(teamId: Long): List<MatchPlayerData>? =
        teamToPlayerList.getIfPresent(teamId)

    fun getPlayerUuid(id: Long): UUID? =
        playerIdToUuid.getIfPresent(id)

    fun getTeamIdInMatch(playerName: String, matchId: Long): Long? =
        playerToTeamInMatch.getIfPresent("$playerName|$matchId")
}