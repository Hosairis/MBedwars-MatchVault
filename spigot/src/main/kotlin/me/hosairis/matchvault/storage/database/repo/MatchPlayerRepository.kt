package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.tables.MatchPlayers
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import me.hosairis.matchvault.storage.database.tables.Players
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchPlayerRepository {

    fun create(data: MatchPlayerData): Long {
        return MatchPlayers.insertAndGetId {
            it[matchId] = EntityID(data.matchId, Matches)
            it[playerId] = EntityID(data.playerId, Players)
            it[teamId] = EntityID(data.teamId, MatchTeams)

            it[kills] = data.kills
            it[finalKills] = data.finalKills
            it[deaths] = data.deaths
            it[bedsDestroyed] = data.bedsDestroyed
            it[topKillStreak] = data.topKillStreak
            it[playTime] = data.playTime

            it[resIron] = data.resIron
            it[resGold] = data.resGold
            it[resDiamond] = data.resDiamond
            it[resEmerald] = data.resEmerald

            it[resIronSpawner] = data.resIronSpawner
            it[resGoldSpawner] = data.resGoldSpawner
            it[resDiamondSpawner] = data.resDiamondSpawner
            it[resEmeraldSpawner] = data.resEmeraldSpawner

            it[won] = data.won
        }.value
    }

    fun update(data: MatchPlayerData): Boolean {
        return MatchPlayers.update({ MatchPlayers.id eq data.id }) {
            it[teamId] = EntityID(data.teamId, MatchTeams)

            it[kills] = data.kills
            it[finalKills] = data.finalKills
            it[deaths] = data.deaths
            it[bedsDestroyed] = data.bedsDestroyed
            it[topKillStreak] = data.topKillStreak
            it[playTime] = data.playTime

            it[resIron] = data.resIron
            it[resGold] = data.resGold
            it[resDiamond] = data.resDiamond
            it[resEmerald] = data.resEmerald

            it[resIronSpawner] = data.resIronSpawner
            it[resGoldSpawner] = data.resGoldSpawner
            it[resDiamondSpawner] = data.resDiamondSpawner
            it[resEmeraldSpawner] = data.resEmeraldSpawner

            it[won] = data.won
        } > 0
    }

    fun updatePartial(
        id: Long,
        kills: Int? = null,
        finalKills: Int? = null,
        deaths: Int? = null,
        bedsDestroyed: Int? = null,
        topKillStreak: Int? = null,
        playTime: Long? = null,
        resIron: Long? = null,
        resGold: Long? = null,
        resDiamond: Long? = null,
        resEmerald: Long? = null,
        resIronSpawner: Long? = null,
        resGoldSpawner: Long? = null,
        resDiamondSpawner: Long? = null,
        resEmeraldSpawner: Long? = null,
        won: Boolean? = null
    ): Boolean {
        return MatchPlayers.update({ MatchPlayers.id eq id }) {
            if (kills != null) it[MatchPlayers.kills] = kills
            if (finalKills != null) it[MatchPlayers.finalKills] = finalKills
            if (deaths != null) it[MatchPlayers.deaths] = deaths
            if (bedsDestroyed != null) it[MatchPlayers.bedsDestroyed] = bedsDestroyed
            if (topKillStreak != null) it[MatchPlayers.topKillStreak] = topKillStreak
            if (playTime != null) it[MatchPlayers.playTime] = playTime
            if (resIron != null && resIron != 0L) it[MatchPlayers.resIron] = MatchPlayers.resIron + resIron
            if (resGold != null && resGold != 0L) it[MatchPlayers.resGold] = MatchPlayers.resGold + resGold
            if (resDiamond != null && resDiamond != 0L) it[MatchPlayers.resDiamond] = MatchPlayers.resDiamond + resDiamond
            if (resEmerald != null && resEmerald != 0L) it[MatchPlayers.resEmerald] = MatchPlayers.resEmerald + resEmerald
            if (resIronSpawner != null && resIronSpawner != 0L) it[MatchPlayers.resIronSpawner] = MatchPlayers.resIronSpawner + resIronSpawner
            if (resGoldSpawner != null && resGoldSpawner != 0L) it[MatchPlayers.resGoldSpawner] = MatchPlayers.resGoldSpawner + resGoldSpawner
            if (resDiamondSpawner != null && resDiamondSpawner != 0L) it[MatchPlayers.resDiamondSpawner] = MatchPlayers.resDiamondSpawner + resDiamondSpawner
            if (resEmeraldSpawner != null && resEmeraldSpawner != 0L) it[MatchPlayers.resEmeraldSpawner] = MatchPlayers.resEmeraldSpawner + resEmeraldSpawner
            if (won != null) it[MatchPlayers.won] = won
        } > 0
    }

    fun delete(id: Long): Boolean {
        return MatchPlayers.deleteWhere { MatchPlayers.id eq id } > 0
    }

    fun read(id: Long, forUpdate: Boolean = false): MatchPlayerData? {
        return MatchPlayers
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchPlayers.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchId(matchId: Long, forUpdate: Boolean = false): List<MatchPlayerData> {
        return MatchPlayers
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchPlayers.matchId eq matchId }
            .orderBy(MatchPlayers.id to SortOrder.ASC)
            .map { it.toData() }
    }

    fun readByPlayerId(playerId: Long, forUpdate: Boolean = false): List<MatchPlayerData> {
        return MatchPlayers
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchPlayers.playerId eq playerId }
            .orderBy(MatchPlayers.matchId to SortOrder.DESC)
            .map { it.toData() }
    }

    fun readByTeamId(teamId: Long, forUpdate: Boolean = false): List<MatchPlayerData> {
        return MatchPlayers
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchPlayers.teamId eq teamId }
            .orderBy(MatchPlayers.matchId to SortOrder.DESC)
            .map { it.toData() }
    }

    fun readByMatchIdAndPlayerId(matchId: Long, playerId: Long, forUpdate: Boolean = false): MatchPlayerData? {
        return MatchPlayers
            .selectAll()
            .withForUpdate(forUpdate)
            .where { (MatchPlayers.matchId eq matchId) and (MatchPlayers.playerId eq playerId) }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    private fun ResultRow.toData(): MatchPlayerData {
        return MatchPlayerData(
            id = this[MatchPlayers.id].value,
            matchId = this[MatchPlayers.matchId].value,
            playerId = this[MatchPlayers.playerId].value,
            teamId = this[MatchPlayers.teamId].value,

            kills = this[MatchPlayers.kills],
            finalKills = this[MatchPlayers.finalKills],
            deaths = this[MatchPlayers.deaths],
            bedsDestroyed = this[MatchPlayers.bedsDestroyed],
            topKillStreak = this[MatchPlayers.topKillStreak],
            playTime = this[MatchPlayers.playTime],

            resIron = this[MatchPlayers.resIron],
            resGold = this[MatchPlayers.resGold],
            resDiamond = this[MatchPlayers.resDiamond],
            resEmerald = this[MatchPlayers.resEmerald],

            resIronSpawner = this[MatchPlayers.resIronSpawner],
            resGoldSpawner = this[MatchPlayers.resGoldSpawner],
            resDiamondSpawner = this[MatchPlayers.resDiamondSpawner],
            resEmeraldSpawner = this[MatchPlayers.resEmeraldSpawner],

            won = this[MatchPlayers.won]
        )
    }

    private fun Query.withForUpdate(forUpdate: Boolean): Query {
        return if (forUpdate) this.forUpdate() else this
    }
}
