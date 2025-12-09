package me.hosairis.matchvault.storage.database.data

import me.hosairis.matchvault.storage.database.MatchPlayers
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

data class MatchPlayerData(
    val matchId: Long,
    val playerId: Long,
    val teamId: Long,
    var kills: Int = 0,
    var finalKills: Int = 0,
    var deaths: Int = 0,
    var bedsDestroyed: Int = 0,
    var resIron: Long = 0,
    var resGold: Long = 0,
    var resDiamond: Long = 0,
    var resEmerald: Long = 0,
    var resIronSpawner: Long = 0,
    var resGoldSpawner: Long = 0,
    var resDiamondSpawner: Long = 0,
    var resEmeraldSpawner: Long = 0,
    var won: Boolean = false
) {
    var id: Long? = null
        private set

    companion object {
        fun read(id: Long): MatchPlayerData? {
            return MatchPlayers
                .selectAll()
                .where { MatchPlayers.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        fun readByMatchId(matchId: Long): List<MatchPlayerData> {
            val matchRef = EntityID(matchId, Matches)
            return MatchPlayers
                .selectAll()
                .where { MatchPlayers.matchId eq matchRef }
                .map { it.toData() }
        }

        fun readByPlayerId(playerId: Long): List<MatchPlayerData> {
            val playerRef = EntityID(playerId, Players)
            return MatchPlayers
                .selectAll()
                .where { MatchPlayers.playerId eq playerRef }
                .map { it.toData() }
        }

        fun readByTeamId(teamId: Long): List<MatchPlayerData> {
            val teamRef = EntityID(teamId, MatchTeams)
            return MatchPlayers
                .selectAll()
                .where { MatchPlayers.teamId eq teamRef }
                .map { it.toData() }
        }

        fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return MatchPlayers.update({ MatchPlayers.id eq id }) { statement ->
                val fetchRow = {
                    MatchPlayers
                        .selectAll()
                        .where { MatchPlayers.id eq id }
                        .forUpdate()
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }

        private fun ResultRow.toData(): MatchPlayerData =
            MatchPlayerData(
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
            ).apply {
                id = this@toData[MatchPlayers.id].value
            }
    }

    fun create(): Boolean {
        val newId = MatchPlayers.insertAndGetId { statement ->
            statement[MatchPlayers.matchId] = EntityID(this@MatchPlayerData.matchId, Matches)
            statement[MatchPlayers.playerId] = EntityID(this@MatchPlayerData.playerId, Players)
            statement[MatchPlayers.teamId] = EntityID(this@MatchPlayerData.teamId, MatchTeams)
            statement[MatchPlayers.kills] = this@MatchPlayerData.kills
            statement[MatchPlayers.finalKills] = this@MatchPlayerData.finalKills
            statement[MatchPlayers.deaths] = this@MatchPlayerData.deaths
            statement[MatchPlayers.bedsDestroyed] = this@MatchPlayerData.bedsDestroyed
            statement[MatchPlayers.resIron] = this@MatchPlayerData.resIron
            statement[MatchPlayers.resGold] = this@MatchPlayerData.resGold
            statement[MatchPlayers.resDiamond] = this@MatchPlayerData.resDiamond
            statement[MatchPlayers.resEmerald] = this@MatchPlayerData.resEmerald
            statement[MatchPlayers.resIronSpawner] = this@MatchPlayerData.resIronSpawner
            statement[MatchPlayers.resGoldSpawner] = this@MatchPlayerData.resGoldSpawner
            statement[MatchPlayers.resDiamondSpawner] = this@MatchPlayerData.resDiamondSpawner
            statement[MatchPlayers.resEmeraldSpawner] = this@MatchPlayerData.resEmeraldSpawner
            statement[MatchPlayers.won] = this@MatchPlayerData.won
        }
        this@MatchPlayerData.id = newId.value
        return true
    }

    fun update(builder: (UpdateBuilder<Int>.(MatchPlayerData) -> Unit)? = null): Boolean {
        val recordId = id ?: return false
        return MatchPlayers.update({ MatchPlayers.id eq recordId }) { statement ->
            if (builder == null) {
                statement[MatchPlayers.kills] = this@MatchPlayerData.kills
                statement[MatchPlayers.finalKills] = this@MatchPlayerData.finalKills
                statement[MatchPlayers.deaths] = this@MatchPlayerData.deaths
                statement[MatchPlayers.bedsDestroyed] = this@MatchPlayerData.bedsDestroyed
                statement[MatchPlayers.resIron] = this@MatchPlayerData.resIron
                statement[MatchPlayers.resGold] = this@MatchPlayerData.resGold
                statement[MatchPlayers.resDiamond] = this@MatchPlayerData.resDiamond
                statement[MatchPlayers.resEmerald] = this@MatchPlayerData.resEmerald
                statement[MatchPlayers.resIronSpawner] = this@MatchPlayerData.resIronSpawner
                statement[MatchPlayers.resGoldSpawner] = this@MatchPlayerData.resGoldSpawner
                statement[MatchPlayers.resDiamondSpawner] = this@MatchPlayerData.resDiamondSpawner
                statement[MatchPlayers.resEmeraldSpawner] = this@MatchPlayerData.resEmeraldSpawner
                statement[MatchPlayers.won] = this@MatchPlayerData.won
            } else {
                builder.invoke(statement, this@MatchPlayerData)
            }
        } > 0
    }

    fun delete(): Boolean {
        val recordId = id ?: return false
        return MatchPlayers.deleteWhere { MatchPlayers.id eq recordId } > 0
    }
}
