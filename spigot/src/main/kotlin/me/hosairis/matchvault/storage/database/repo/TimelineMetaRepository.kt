package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.TimelineMetaData
import me.hosairis.matchvault.storage.database.tables.TimelineMetas
import me.hosairis.matchvault.storage.database.tables.Timelines
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class TimelineMetaRepository {

    fun create(data: TimelineMetaData): Long =
        TimelineMetas.insertAndGetId {
            it[timelineId] = EntityID(data.timelineId, Timelines)
            it[key] = data.key
            it[value] = data.value
        }.value

    fun read(id: Long): TimelineMetaData? =
        TimelineMetas
            .selectAll()
            .where { TimelineMetas.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readByTimelineId(timelineId: Long): List<TimelineMetaData> =
        TimelineMetas
            .selectAll()
            .where { TimelineMetas.timelineId eq timelineId }
            .orderBy(TimelineMetas.id to SortOrder.ASC)
            .map { it.toData() }

    fun update(data: TimelineMetaData): Boolean =
        TimelineMetas.update({ TimelineMetas.id eq data.id }) { st ->
            st[key] = data.key
            st[value] = data.value
        } > 0

    fun delete(id: Long): Boolean =
        TimelineMetas.deleteWhere { TimelineMetas.id eq id } > 0

    fun deleteByTimelineId(timelineId: Long): Int =
        TimelineMetas.deleteWhere { TimelineMetas.timelineId eq timelineId }

    private fun ResultRow.toData(): TimelineMetaData = TimelineMetaData(
        id = this[TimelineMetas.id].value,
        timelineId = this[TimelineMetas.timelineId].value,
        key = this[TimelineMetas.key],
        value = this[TimelineMetas.value]
    )
}
