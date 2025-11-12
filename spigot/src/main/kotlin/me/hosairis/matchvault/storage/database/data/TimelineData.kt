package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.EventType
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.Timelines
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

data class TimelineData(
    val matchId: Long,
    val playerId: Long? = null,
    val targetId: Long? = null,
    val teamId: Long? = null,
    var timestamp: Long,
    var type: EventType
) {
    var id: Long? = null
        private set

    var metas: MutableList<TimelineMetaData> = mutableListOf()
        private set

    companion object {
        suspend fun read(id: Long): TimelineData? = withContext(Dispatchers.IO) {
            transaction {
                Timelines
                    .selectAll()
                    .where { Timelines.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByMatchId(matchId: Long): List<TimelineData> = withContext(Dispatchers.IO) {
            transaction {
                val matchRef = EntityID(matchId, Matches)
                Timelines
                    .selectAll()
                    .where { Timelines.matchId eq matchRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByPlayerId(playerId: Long): List<TimelineData> = withContext(Dispatchers.IO) {
            transaction {
                val playerRef = EntityID(playerId, Players)
                Timelines
                    .selectAll()
                    .where { Timelines.playerId eq playerRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByTargetId(targetId: Long): List<TimelineData> = withContext(Dispatchers.IO) {
            transaction {
                val targetRef = EntityID(targetId, Players)
                Timelines
                    .selectAll()
                    .where { Timelines.targetId eq targetRef }
                    .map { it.toData() }
            }
        }

        suspend fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                try {
                    Timelines.update({ Timelines.id eq id }) { statement ->
                        val fetchRow = {
                            Timelines
                                .selectAll()
                                .where { Timelines.id eq id }
                                .limit(1)
                                .firstOrNull()
                        }
                        builder(statement, fetchRow)
                    } > 0
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    false
                }
            }
        }

        suspend fun updateByMatchId(
            matchId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                try {
                    val matchRef = EntityID(matchId, Matches)
                    Timelines.update({ Timelines.matchId eq matchRef }) { statement ->
                        val fetchRow = {
                            Timelines
                                .selectAll()
                                .where { Timelines.matchId eq matchRef }
                                .limit(1)
                                .firstOrNull()
                        }
                        builder(statement, fetchRow)
                    } > 0
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    false
                }
            }
        }

        suspend fun updateByPlayerId(
            playerId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                try {
                    val playerRef = EntityID(playerId, Players)
                    Timelines.update({ Timelines.playerId eq playerRef }) { statement ->
                        val fetchRow = {
                            Timelines
                                .selectAll()
                                .where { Timelines.playerId eq playerRef }
                                .limit(1)
                                .firstOrNull()
                        }
                        builder(statement, fetchRow)
                    } > 0
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    false
                }
            }
        }

        suspend fun updateByTargetId(
            targetId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                try {
                    val targetRef = EntityID(targetId, Players)
                    Timelines.update({ Timelines.targetId eq targetRef }) { statement ->
                        val fetchRow = {
                            Timelines
                                .selectAll()
                                .where { Timelines.targetId eq targetRef }
                                .limit(1)
                                .firstOrNull()
                        }
                        builder(statement, fetchRow)
                    } > 0
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    false
                }
            }
        }

        private fun ResultRow.toData(): TimelineData =
            TimelineData(
                matchId = this[Timelines.matchId].value,
                playerId = this[Timelines.playerId]?.value,
                targetId = this[Timelines.targetId]?.value,
                teamId = this[Timelines.teamId]?.value,
                timestamp = this[Timelines.timestamp],
                type = this[Timelines.type]
            ).apply {
                id = this@toData[Timelines.id].value
            }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                val newId = Timelines.insertAndGetId { statement ->
                    statement[Timelines.matchId] = EntityID(this@TimelineData.matchId, Matches)
                    statement[Timelines.playerId] = this@TimelineData.playerId?.let { EntityID(it, Players) }
                    statement[Timelines.targetId] = this@TimelineData.targetId?.let { EntityID(it, Players) }
                    statement[Timelines.teamId] = this@TimelineData.teamId?.let { EntityID(it, MatchTeams) }
                    statement[Timelines.timestamp] = this@TimelineData.timestamp
                    statement[Timelines.type] = this@TimelineData.type
                }
                this@TimelineData.id = newId.value
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(builder: (UpdateBuilder<Int>.(TimelineData) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            try {
                Timelines.update({ Timelines.id eq recordId }) { statement ->
                    if (builder == null) {
                        statement[Timelines.timestamp] = this@TimelineData.timestamp
                        statement[Timelines.type] = this@TimelineData.type
                    } else {
                        builder.invoke(statement, this@TimelineData)
                    }
                } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            try {
                Timelines.deleteWhere { Timelines.id eq recordId } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun loadMetas() {
        val timelineId = id ?: return
        metas.clear()
        metas.addAll(TimelineMetaData.readByTimelineId(timelineId))
    }
}
