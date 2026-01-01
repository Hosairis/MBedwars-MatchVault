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
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
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
        val exists = MatchPlayers
            .select(MatchPlayers.id)
            .where { MatchPlayers.id eq data.id }
            .forUpdate()
            .limit(1)
            .any()

        if (!exists) return false

        return MatchPlayers.update({ MatchPlayers.id eq data.id }) {
            it[teamId] = EntityID(data.teamId, MatchTeams)

            it[kills] = data.kills
            it[finalKills] = data.finalKills
            it[deaths] = data.deaths
            it[bedsDestroyed] = data.bedsDestroyed

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

    fun delete(id: Long): Boolean {
        return MatchPlayers.deleteWhere { MatchPlayers.id eq id } > 0
    }

    fun read(id: Long): MatchPlayerData? {
        return MatchPlayers
            .selectAll()
            .where { MatchPlayers.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchId(matchId: Long): List<MatchPlayerData> {
        return MatchPlayers
            .selectAll()
            .where { MatchPlayers.matchId eq matchId }
            .orderBy(MatchPlayers.id to SortOrder.ASC)
            .map { it.toData() }
    }

    fun readByPlayerId(playerId: Long): List<MatchPlayerData> {
        return MatchPlayers
            .selectAll()
            .where { MatchPlayers.playerId eq playerId }
            .orderBy(MatchPlayers.matchId to SortOrder.DESC)
            .map { it.toData() }
    }

    fun readByMatchIdAndPlayerId(matchId: Long, playerId: Long): MatchPlayerData? {
        return MatchPlayers
            .selectAll()
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
}
