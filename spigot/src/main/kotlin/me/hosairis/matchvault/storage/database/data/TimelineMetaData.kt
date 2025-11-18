package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.TimelineMetas
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

data class TimelineMetaData(
    val timelineId: Long,
    var key: String,
    var value: String
) {
    var id: Long? = null
        private set

    companion object {
        suspend fun read(id: Long): TimelineMetaData? = withContext(Dispatchers.IO) {
            transaction {
                TimelineMetas
                    .selectAll()
                    .where { TimelineMetas.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByTimelineId(timelineId: Long): List<TimelineMetaData> = withContext(Dispatchers.IO) {
            transaction {
                val timelineRef = EntityID(timelineId, Timelines)
                TimelineMetas
                    .selectAll()
                    .where { TimelineMetas.timelineId eq timelineRef }
                    .map { it.toData() }
            }
        }

        suspend fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                TimelineMetas.update({ TimelineMetas.id eq id }) { statement ->
                    val fetchRow = {
                        TimelineMetas
                            .selectAll()
                            .where { TimelineMetas.id eq id }
                            .limit(1)
                            .firstOrNull()
                    }
                    builder(statement, fetchRow)
                } > 0
            }
        }

        suspend fun updateByTimelineId(
            timelineId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                val timelineRef = EntityID(timelineId, Timelines)
                TimelineMetas.update({ TimelineMetas.timelineId eq timelineRef }) { statement ->
                    val fetchRow = {
                        TimelineMetas
                            .selectAll()
                            .where { TimelineMetas.timelineId eq timelineRef }
                            .limit(1)
                            .firstOrNull()
                    }
                    builder(statement, fetchRow)
                } > 0
            }
        }

        private fun ResultRow.toData(): TimelineMetaData =
            TimelineMetaData(
                timelineId = this[TimelineMetas.timelineId].value,
                key = this[TimelineMetas.key],
                value = this[TimelineMetas.value]
            ).apply {
                id = this@toData[TimelineMetas.id].value
            }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            val newId = TimelineMetas.insertAndGetId { statement ->
                statement[TimelineMetas.timelineId] = EntityID(this@TimelineMetaData.timelineId, Timelines)
                statement[TimelineMetas.key] = this@TimelineMetaData.key
                statement[TimelineMetas.value] = this@TimelineMetaData.value
            }
            this@TimelineMetaData.id = newId.value
            true
        }
    }

    suspend fun update(builder: (UpdateBuilder<Int>.(TimelineMetaData) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            TimelineMetas.update({ TimelineMetas.id eq recordId }) { statement ->
                if (builder == null) {
                    statement[TimelineMetas.key] = this@TimelineMetaData.key
                    statement[TimelineMetas.value] = this@TimelineMetaData.value
                } else {
                    builder.invoke(statement, this@TimelineMetaData)
                }
            } > 0
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            TimelineMetas.deleteWhere { TimelineMetas.id eq recordId } > 0
        }
    }
}
