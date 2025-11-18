package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.PlayerStats
import me.hosairis.matchvault.storage.database.Players
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

data class PlayerStatsData(
    val playerId: Long,
    var matchesPlayed: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0,
    var kills: Int = 0,
    var finalKills: Int = 0,
    var deaths: Int = 0,
    var bedsBroken: Int = 0,
    var resIron: Long = 0,
    var resGold: Long = 0,
    var resDiamond: Long = 0,
    var resEmerald: Long = 0,
    var resIronSpawner: Long = 0,
    var resGoldSpawner: Long = 0,
    var resDiamondSpawner: Long = 0,
    var resEmeraldSpawner: Long = 0
) {
    var id: Long? = null
        private set

    companion object {
        suspend fun read(id: Long): PlayerStatsData? = withContext(Dispatchers.IO) {
            transaction {
                PlayerStats
                    .selectAll()
                    .where { PlayerStats.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByPlayerId(playerId: Long): PlayerStatsData? = withContext(Dispatchers.IO) {
            transaction {
                val playerRef = EntityID(playerId, Players)
                PlayerStats
                    .selectAll()
                    .where { PlayerStats.playerId eq playerRef }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                PlayerStats.update({ PlayerStats.id eq id }) { statement ->
                    val fetchRow = {
                        PlayerStats
                            .selectAll()
                            .where { PlayerStats.id eq id }
                            .limit(1)
                            .firstOrNull()
                    }
                    builder(statement, fetchRow)
                } > 0
            }
        }

        suspend fun updateByPlayerId(
            playerId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            val playerRef = EntityID(playerId, Players)
            transaction {
                PlayerStats.update({ PlayerStats.playerId eq playerRef }) { statement ->
                    val fetchRow = {
                        PlayerStats
                            .selectAll()
                            .where { PlayerStats.playerId eq playerRef }
                            .limit(1)
                            .firstOrNull()
                    }
                    builder(statement, fetchRow)
                } > 0
            }
        }

        private fun ResultRow.toData(): PlayerStatsData =
            PlayerStatsData(
                playerId = this[PlayerStats.playerId].value,
                matchesPlayed = this[PlayerStats.matchesPlayed],
                wins = this[PlayerStats.wins],
                losses = this[PlayerStats.losses],
                kills = this[PlayerStats.kills],
                finalKills = this[PlayerStats.finalKills],
                deaths = this[PlayerStats.deaths],
                bedsBroken = this[PlayerStats.bedsBroken],
                resIron = this[PlayerStats.resIron],
                resGold = this[PlayerStats.resGold],
                resDiamond = this[PlayerStats.resDiamond],
                resEmerald = this[PlayerStats.resEmerald],
                resIronSpawner = this[PlayerStats.resIronSpawner],
                resGoldSpawner = this[PlayerStats.resGoldSpawner],
                resDiamondSpawner = this[PlayerStats.resDiamondSpawner],
                resEmeraldSpawner = this[PlayerStats.resEmeraldSpawner]
            ).apply {
                id = this@toData[PlayerStats.id].value
            }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            val newId = PlayerStats.insertAndGetId { statement ->
                statement[PlayerStats.playerId] = EntityID(this@PlayerStatsData.playerId, Players)
                statement[PlayerStats.matchesPlayed] = this@PlayerStatsData.matchesPlayed
                statement[PlayerStats.wins] = this@PlayerStatsData.wins
                statement[PlayerStats.losses] = this@PlayerStatsData.losses
                statement[PlayerStats.kills] = this@PlayerStatsData.kills
                statement[PlayerStats.finalKills] = this@PlayerStatsData.finalKills
                statement[PlayerStats.deaths] = this@PlayerStatsData.deaths
                statement[PlayerStats.bedsBroken] = this@PlayerStatsData.bedsBroken
                statement[PlayerStats.resIron] = this@PlayerStatsData.resIron
                statement[PlayerStats.resGold] = this@PlayerStatsData.resGold
                statement[PlayerStats.resDiamond] = this@PlayerStatsData.resDiamond
                statement[PlayerStats.resEmerald] = this@PlayerStatsData.resEmerald
                statement[PlayerStats.resIronSpawner] = this@PlayerStatsData.resIronSpawner
                statement[PlayerStats.resGoldSpawner] = this@PlayerStatsData.resGoldSpawner
                statement[PlayerStats.resDiamondSpawner] = this@PlayerStatsData.resDiamondSpawner
                statement[PlayerStats.resEmeraldSpawner] = this@PlayerStatsData.resEmeraldSpawner
            }
            this@PlayerStatsData.id = newId.value
            true
        }
    }

    suspend fun update(builder: (UpdateBuilder<Int>.(PlayerStatsData) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val playerRef = EntityID(playerId, Players)
        transaction {
            PlayerStats.update({ PlayerStats.playerId eq playerRef }) { statement ->
                if (builder == null) {
                    statement[PlayerStats.matchesPlayed] = this@PlayerStatsData.matchesPlayed
                    statement[PlayerStats.wins] = this@PlayerStatsData.wins
                    statement[PlayerStats.losses] = this@PlayerStatsData.losses
                    statement[PlayerStats.kills] = this@PlayerStatsData.kills
                    statement[PlayerStats.finalKills] = this@PlayerStatsData.finalKills
                    statement[PlayerStats.deaths] = this@PlayerStatsData.deaths
                    statement[PlayerStats.bedsBroken] = this@PlayerStatsData.bedsBroken
                    statement[PlayerStats.resIron] = this@PlayerStatsData.resIron
                    statement[PlayerStats.resGold] = this@PlayerStatsData.resGold
                    statement[PlayerStats.resDiamond] = this@PlayerStatsData.resDiamond
                    statement[PlayerStats.resEmerald] = this@PlayerStatsData.resEmerald
                    statement[PlayerStats.resIronSpawner] = this@PlayerStatsData.resIronSpawner
                    statement[PlayerStats.resGoldSpawner] = this@PlayerStatsData.resGoldSpawner
                    statement[PlayerStats.resDiamondSpawner] = this@PlayerStatsData.resDiamondSpawner
                    statement[PlayerStats.resEmeraldSpawner] = this@PlayerStatsData.resEmeraldSpawner
                } else {
                    builder.invoke(statement, this@PlayerStatsData)
                }
            } > 0
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val playerRef = EntityID(playerId, Players)
        transaction {
            PlayerStats.deleteWhere { PlayerStats.playerId eq playerRef } > 0
        }
    }
}
