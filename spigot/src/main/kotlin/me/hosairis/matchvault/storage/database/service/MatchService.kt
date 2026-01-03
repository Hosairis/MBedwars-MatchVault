package me.hosairis.matchvault.storage.database.service

import de.marcely.bedwars.api.arena.Team
import me.hosairis.matchvault.storage.database.enums.MatchStatus
import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.repo.MatchPlayerRepository
import me.hosairis.matchvault.storage.database.repo.MatchRepository
import me.hosairis.matchvault.storage.database.repo.MatchTeamRepository
import me.hosairis.matchvault.storage.database.repo.PlayerRepository
import org.bukkit.Material
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class MatchService(
    private val matchRepo: MatchRepository,
    private val teamRepo: MatchTeamRepository,
    private val matchPlayerRepo: MatchPlayerRepository,
    private val playerRepo: PlayerRepository
) {

    /**
     * Create match row, then create its teams and match_players rows.
     */
    fun startMatch(
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

        matchId
    }

    /**
     * Finish a match: set ENDED, endedAt, duration, winner/tie.
     */
    fun endMatch(
        matchId: Long,
        endedAt: Long,
        isTie: Boolean,
        winnerTeamName: String?,
        winnersUuids: List<UUID>
    ): Boolean = transaction {
        val match = matchRepo.read(matchId) ?: return@transaction false
        val duration = endedAt - match.startedAt
        val teams = teamRepo.readByMatchId(matchId)
        val winnerTeamId: Long? =
            if (isTie || winnerTeamName == null) null
            else teams.firstOrNull { it.team == winnerTeamName }?.id

        matchRepo.update(
            match.copy(
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
                val mp = matchPlayerRepo.readByMatchIdAndPlayerId(matchId, playerId) ?: continue

                if (!mp.won) {
                    matchPlayerRepo.update(mp.copy(won = true))
                }
            }
        }

        true
    }

    fun abortMatch(matchId: Long, abortedAt: Long = System.currentTimeMillis()): Boolean = transaction {
        val match = matchRepo.read(matchId) ?: return@transaction false
        val duration = abortedAt - match.startedAt

        matchRepo.update(
            match.copy(
                endedAt = abortedAt,
                duration = duration,
                status = MatchStatus.ABORTED
            )
        )
    }

    /**
     * For startup purpose only, in case server crashed mid-match
     */
    fun abortOngoingMatchesOnStartup(server: String): Int = transaction {
        matchRepo.abortOngoingByServer(server)
    }

    fun recordBedBreak(
        matchId: Long,
        teamName: String,
        timestamp: Long
    ): Boolean = transaction {
        val team = teamRepo.readByMatchIdAndTeam(matchId, teamName) ?: return@transaction false
        teamRepo.update(team.copy(bedDestroyedAt = timestamp))
    }

    fun recordTeamElimination(
        matchId: Long,
        teamName: String,
        placement: Int,
        timestamp: Long
    ): Boolean = transaction {
        val team = teamRepo.readByMatchIdAndTeam(matchId, teamName) ?: return@transaction false
        teamRepo.update(
            team.copy(
                eliminatedAt = timestamp,
                finalPlacement = placement
            )
        )
    }

    fun updateMatchPlayerStat(
        matchId: Long,
        playerUuid: UUID,
        statKey: String,
        newValue: Int
    ): Boolean = transaction {
        val playerId = playerRepo.readByUuid(playerUuid)?.id ?: return@transaction false
        val mp = matchPlayerRepo.readByMatchIdAndPlayerId(matchId, playerId) ?: return@transaction false

        when (statKey) {
            "bedwars:kills" -> matchPlayerRepo.update(mp.copy(kills = newValue))
            "bedwars:final_kills" -> matchPlayerRepo.update(mp.copy(finalKills = newValue))
            "bedwars:deaths" -> matchPlayerRepo.update(mp.copy(deaths = newValue))
            "bedwars:beds_destroyed" -> matchPlayerRepo.update(mp.copy(bedsDestroyed = newValue))
            else -> return@transaction false
        }
    }

    fun incrementResourcePickup(
        matchId: Long,
        playerId: Long,
        resIron: Long = 0,
        resGold: Long = 0,
        resDiamond: Long = 0,
        resEmerald: Long = 0,
        resIronSpawner: Long = 0,
        resGoldSpawner: Long = 0,
        resDiamondSpawner: Long = 0,
        resEmeraldSpawner: Long = 0,
    ): Boolean = transaction {
        val matchPlayerData = matchPlayerRepo.readByMatchIdAndPlayerId(matchId = matchId, playerId = playerId) ?: return@transaction false
        
        matchPlayerRepo.update(matchPlayerData.copy(
            resIron = matchPlayerData.resIron + resIron,
            resGold = matchPlayerData.resGold + resGold,
            resDiamond = matchPlayerData.resDiamond + resDiamond,
            resEmerald = matchPlayerData.resEmerald + resEmerald,
            resIronSpawner = matchPlayerData.resIronSpawner + resIronSpawner,
            resGoldSpawner = matchPlayerData.resGoldSpawner + resGoldSpawner,
            resDiamondSpawner = matchPlayerData.resDiamondSpawner + resDiamondSpawner,
            resEmeraldSpawner = matchPlayerData.resEmeraldSpawner + resEmeraldSpawner
        ))
    }

    fun read(matchId: Long): MatchData? = transaction {
        matchRepo.read(matchId)
    }

    fun readTeamByMatchIdAndTeam(matchId: Long, teamName: String): Long? = transaction {
        teamRepo.readByMatchIdAndTeam(matchId, teamName)?.id
    }

    /**
     * list matches a player participated in.
     */
    fun readMatchesOfPlayer(uuid: UUID): List<MatchData> = transaction {
        val playerId = playerRepo.readByUuid(uuid)?.id ?: return@transaction emptyList()
        val entries = matchPlayerRepo.readByPlayerId(playerId)

        entries.mapNotNull { e -> matchRepo.read(e.matchId) }
    }
}
