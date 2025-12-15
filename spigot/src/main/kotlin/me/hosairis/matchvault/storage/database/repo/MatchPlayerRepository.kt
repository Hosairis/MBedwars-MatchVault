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
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchPlayerRepository {

    fun create(data: MatchPlayerData): Long =
        MatchPlayers.insertAndGetId {
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

    fun read(id: Long): MatchPlayerData? =
        MatchPlayers
            .selectAll()
            .where { MatchPlayers.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readByMatchId(matchId: Long): List<MatchPlayerData> =
        MatchPlayers
            .selectAll()
            .where { MatchPlayers.matchId eq matchId }
            .orderBy(MatchPlayers.id to SortOrder.ASC)
            .map { it.toData() }

    fun readByPlayerId(playerId: Long): List<MatchPlayerData> =
        MatchPlayers
            .selectAll()
            .where { MatchPlayers.playerId eq playerId }
            .orderBy(MatchPlayers.matchId to SortOrder.DESC)
            .map { it.toData() }

    fun update(data: MatchPlayerData): Boolean =
        MatchPlayers.update({ MatchPlayers.id eq data.id }) {
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

    fun incrementResources(
        matchId: Long,
        playerId: Long,
        iron: Long = 0,
        gold: Long = 0,
        diamond: Long = 0,
        emerald: Long = 0,
        ironSpawner: Long = 0,
        goldSpawner: Long = 0,
        diamondSpawner: Long = 0,
        emeraldSpawner: Long = 0
    ): Int {
        // Nothing to update
        if (
            iron == 0L && gold == 0L && diamond == 0L && emerald == 0L &&
            ironSpawner == 0L && goldSpawner == 0L && diamondSpawner == 0L && emeraldSpawner == 0L
        ) return 0

        return MatchPlayers.update(
            where = { (MatchPlayers.matchId eq matchId) and (MatchPlayers.playerId eq playerId) }
        ) { st ->
            // IMPORTANT: these use Exposed SQL expressions (Column + value)
            if (iron != 0L) st[MatchPlayers.resIron] = MatchPlayers.resIron + iron
            if (gold != 0L) st[MatchPlayers.resGold] = MatchPlayers.resGold + gold
            if (diamond != 0L) st[MatchPlayers.resDiamond] = MatchPlayers.resDiamond + diamond
            if (emerald != 0L) st[MatchPlayers.resEmerald] = MatchPlayers.resEmerald + emerald

            if (ironSpawner != 0L) st[MatchPlayers.resIronSpawner] = MatchPlayers.resIronSpawner + ironSpawner
            if (goldSpawner != 0L) st[MatchPlayers.resGoldSpawner] = MatchPlayers.resGoldSpawner + goldSpawner
            if (diamondSpawner != 0L) st[MatchPlayers.resDiamondSpawner] = MatchPlayers.resDiamondSpawner + diamondSpawner
            if (emeraldSpawner != 0L) st[MatchPlayers.resEmeraldSpawner] = MatchPlayers.resEmeraldSpawner + emeraldSpawner
        }
    }

    fun delete(id: Long): Boolean =
        MatchPlayers.deleteWhere { MatchPlayers.id eq id } > 0

    fun readByMatchIdAndPlayerId(matchId: Long, playerId: Long): MatchPlayerData? =
        MatchPlayers
            .selectAll()
            .where { (MatchPlayers.matchId eq matchId) and (MatchPlayers.playerId eq playerId) }
            .limit(1)
            .firstOrNull()
            ?.toData()

    private fun ResultRow.toData(): MatchPlayerData = MatchPlayerData(
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
