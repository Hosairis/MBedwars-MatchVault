package me.hosairis.matchvault.storage.database.service

import de.marcely.bedwars.api.arena.Arena
import de.marcely.bedwars.api.arena.Team
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.cache.MatchCache
import me.hosairis.matchvault.storage.database.cache.MatchHistoryCache
import me.hosairis.matchvault.storage.database.enums.MatchStatus
import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.repo.MatchPlayerRepository
import me.hosairis.matchvault.storage.database.repo.MatchRepository
import me.hosairis.matchvault.storage.database.repo.MatchTeamRepository
import me.hosairis.matchvault.storage.database.repo.PlayerRepository
import me.hosairis.matchvault.util.Log
import org.bukkit.Material
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

object MatchService {
    private val matchRepo = MatchRepository()
    private val teamRepo = MatchTeamRepository()
    private val matchPlayerRepo = MatchPlayerRepository()
    private val playerRepo = PlayerRepository()

    fun startMatch(
        arena: Arena,
        arenaName: String,
        mode: Int,
        startedAt: Long,
        teamMap: Map<Team, List<UUID>>
    ): Long = transaction {

        val matchId = matchRepo.create(MatchData(
            arenaName = arenaName,
            mode = mode,
            startedAt = startedAt
        ))

        teamMap.forEach { (team, uuids) ->
            val teamId = teamRepo.create(MatchTeamData(
                matchId = matchId,
                team = team.name
            ))

            uuids.forEach { uuid ->
                val playerId = playerRepo.readIdByUuid(uuid) ?: return@forEach
                matchPlayerRepo.create(MatchPlayerData(
                    matchId = matchId,
                    playerId = playerId,
                    teamId = teamId
                ))
            }
        }

        MatchCache.put(arena, matchId)
        matchId
    }

    fun endMatch(
        arena: Arena,
        endedAt: Long,
        isTie: Boolean,
        winnerTeamName: String?,
        winnersUuids: List<UUID>
    ): Boolean = transaction {

        val matchData = MatchCache.getId(arena)?.let { id ->
            matchRepo.read(id)
        } ?: return@transaction false
        val duration = endedAt - matchData.startedAt
        val teams = teamRepo.readByMatchId(matchData.id!!)
        val winnerTeamId: Long? =
            if (isTie || winnerTeamName == null) null
            else teams.firstOrNull { it.team == winnerTeamName }?.id

        matchRepo.update(
            matchData.copy(
                endedAt = endedAt,
                duration = duration,
                status = MatchStatus.ENDED,
                isTie = isTie,
                winnerTeamId = winnerTeamId
            )
        )

        if (!isTie && winnerTeamId != null) {
            val winnerTeam = teams.firstOrNull { it.id == winnerTeamId }
            if (winnerTeam != null) {
                teamRepo.update(winnerTeam.copy(finalPlacement = 1))
            }
        } else if (isTie) {
            // any team still missing placement -> 2
            teams.filter { it.finalPlacement == null }.forEach { t ->
                teamRepo.update(t.copy(finalPlacement = 2))
            }
        }

        if (winnersUuids.isNotEmpty()) {
            for (uuid in winnersUuids) {
                val playerId = playerRepo.readByUuid(uuid)?.id ?: continue
                val mp = matchPlayerRepo.readByMatchIdAndPlayerId(matchData.id, playerId) ?: continue

                if (!mp.won) {
                    matchPlayerRepo.update(mp.copy(won = true))
                }
            }
        }

        MatchCache.remove(arena)
        true
    }

    fun abortMatch(
        arena: Arena,
        abortedAt: Long = System.currentTimeMillis()
    ): Boolean = transaction {

        val matchData = MatchCache.getId(arena)?.let { id ->
            matchRepo.read(id)
        } ?: return@transaction false
        val duration = abortedAt - matchData.startedAt

        val result = matchRepo.update(
            matchData.copy(
                endedAt = abortedAt,
                duration = duration,
                status = MatchStatus.ABORTED
            )
        )

        MatchCache.remove(arena)
        result
    }

    /**
     * For startup purpose only, in case server crashed mid-match
     */
    fun abortOngoingMatchesOnStartup(server: String): Int = transaction {
        matchRepo.abortOngoingByServer(server)
    }

    fun recordBedBreak(
        arena: Arena,
        teamName: String,
        timestamp: Long
    ): Boolean = transaction {

        val matchData = MatchCache.getId(arena)?.let { id ->
            matchRepo.read(id)
        } ?: return@transaction false
        val team = teamRepo.readByMatchIdAndTeam(matchData.id!!, teamName) ?: return@transaction false
        teamRepo.update(team.copy(bedDestroyedAt = timestamp))
    }

    fun recordTeamElimination(
        arena: Arena,
        teamName: String,
        placement: Int,
        timestamp: Long
    ): Boolean = transaction {

        val matchData = MatchCache.getId(arena)?.let { id ->
            matchRepo.read(id)
        } ?: return@transaction false
        val team = teamRepo.readByMatchIdAndTeam(matchData.id!!, teamName) ?: return@transaction false
        teamRepo.update(
            team.copy(
                eliminatedAt = timestamp,
                finalPlacement = placement
            )
        )
    }

    fun updateMatchPlayerStat(
        arena: Arena,
        playerUuid: UUID,
        statKey: String,
        newValue: Int
    ): Boolean = transaction {

        val matchData = MatchCache.getId(arena)?.let { id ->
            matchRepo.read(id)
        } ?: return@transaction false
        val playerId = playerRepo.readByUuid(playerUuid)?.id ?: return@transaction false
        val mp = matchPlayerRepo.readByMatchIdAndPlayerId(matchData.id!!, playerId) ?: return@transaction false

        when (statKey) {
            "bedwars:kills" -> matchPlayerRepo.updatePartial(id = mp.id!!, kills = newValue)
            "bedwars:final_kills" -> matchPlayerRepo.updatePartial(id = mp.id!!, finalKills = newValue)
            "bedwars:deaths" -> matchPlayerRepo.updatePartial(id = mp.id!!, deaths = newValue)
            "bedwars:beds_destroyed" -> matchPlayerRepo.updatePartial(id = mp.id!!, bedsDestroyed = newValue)
            else -> return@transaction false
        }
    }

    fun updateResourcePickup(
        arena: Arena,
        playerUuid: UUID,
        material: Material,
        amount: Long,
        fromSpawner: Boolean
    ): Boolean = transaction {

        val matchData = MatchCache.getId(arena)?.let { id ->
            matchRepo.read(id)
        } ?: return@transaction false
        val playerId = playerRepo.readByUuid(playerUuid)?.id ?: return@transaction false
        val matchPlayerData = matchPlayerRepo.readByMatchIdAndPlayerId(
            matchId = matchData.id!!,
            playerId = playerId,
            forUpdate = true
        ) ?: return@transaction false
        val spawnerAmount = if (fromSpawner) amount else 0

        when (material) {
            Config.values.allowedMaterials[0] -> {
                Log.info("update resource iron(x$amount)")

                matchPlayerRepo.updatePartial(
                    id = matchPlayerData.id!!,
                    resIron = amount,
                    resIronSpawner = spawnerAmount
                )
            }
            Config.values.allowedMaterials[1] -> {
                Log.info("update resource gold(x$amount)")

                matchPlayerRepo.updatePartial(
                    id = matchPlayerData.id!!,
                    resGold = amount,
                    resGoldSpawner = spawnerAmount
                )
            }
            Config.values.allowedMaterials[2] -> {
                Log.info("update resource diamond(x$amount)")

                matchPlayerRepo.updatePartial(
                    id = matchPlayerData.id!!,
                    resDiamond = amount,
                    resDiamondSpawner = spawnerAmount
                )
            }
            Config.values.allowedMaterials[3] -> {
                Log.info("update resource emerald(x$amount)")

                matchPlayerRepo.updatePartial(
                    id = matchPlayerData.id!!,
                    resEmerald = amount,
                    resEmeraldSpawner = spawnerAmount
                )
            }
            else -> false
        }
    }

    fun readMatch(arena: Arena): MatchData? = transaction {
        readMatchId(arena)?.let { matchRepo.read(it) }
    }

    fun readMatchId(arena: Arena): Long? = MatchCache.getId(arena)

    fun readTeamByMatchIdAndTeam(matchId: Long, teamName: String): Long? = transaction {
        teamRepo.readByMatchIdAndTeam(matchId, teamName)?.id
    }

    /**
     * list matches a player participated in.
     */
    fun readMatchesOfPlayer(playerName: String): Map<MatchData, Boolean> {
        return MatchHistoryCache.getMatchList(playerName) ?: transaction {
            val playerId = PlayerService.readIdByName(playerName) ?: return@transaction emptyMap()

            val result = mutableMapOf<MatchData, Boolean>()
            for (entry in matchPlayerRepo.readByPlayerId(playerId)) {
                val match = matchRepo.read(entry.matchId) ?: continue
                result[match] = entry.won
                MatchHistoryCache.putPlayerTeamIdInMatch(playerName, entry.matchId, entry.teamId)
            }

            MatchHistoryCache.putMatchList(playerName, result)
            result
        }
    }

    fun readTeamsOfMatch(matchId: Long): List<MatchTeamData> {
        return MatchHistoryCache.getTeamList(matchId) ?: transaction {
            teamRepo.readByMatchId(matchId).also { MatchHistoryCache.putTeamList(matchId, it) }
        }
    }

    fun readPlayersOfTeam(teamId: Long): List<MatchPlayerData> {
        return MatchHistoryCache.getPlayerList(teamId) ?: transaction {
            matchPlayerRepo.readByTeamId(teamId).also { MatchHistoryCache.putPlayerList(teamId, it) }
        }
    }

    fun readTeamIdOfPlayer(matchId: Long, playerName: String): Long? {
        return MatchHistoryCache.getTeamIdInMatch(playerName, matchId) ?: transaction {
            val playerId = PlayerService.readIdByName(playerName) ?: return@transaction null
            val matchPlayer = matchPlayerRepo.readByMatchIdAndPlayerId(matchId, playerId) ?: return@transaction null

            MatchHistoryCache.putPlayerTeamIdInMatch(playerName, matchId, matchPlayer.teamId)
            matchPlayer.teamId
        }
    }
}
