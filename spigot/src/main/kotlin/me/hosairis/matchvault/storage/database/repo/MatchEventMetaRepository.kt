package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.MatchEventMetaData
import me.hosairis.matchvault.storage.database.tables.MatchEventMetas
import me.hosairis.matchvault.storage.database.tables.MatchEvents
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchEventMetaRepository {

    fun create(data: MatchEventMetaData): Long =
        MatchEventMetas.insertAndGetId {
            it[matchEventId] = EntityID(data.timelineId, MatchEvents)
            it[key] = data.key
            it[value] = data.value
        }.value

    fun read(id: Long): MatchEventMetaData? =
        MatchEventMetas
            .selectAll()
            .where { MatchEventMetas.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readByTimelineId(timelineId: Long): List<MatchEventMetaData> =
        MatchEventMetas
            .selectAll()
            .where { MatchEventMetas.matchEventId eq timelineId }
            .orderBy(MatchEventMetas.id to SortOrder.ASC)
            .map { it.toData() }

    fun update(data: MatchEventMetaData): Boolean =
        MatchEventMetas.update({ MatchEventMetas.id eq data.id }) { st ->
            st[key] = data.key
            st[value] = data.value
        } > 0

    fun delete(id: Long): Boolean =
        MatchEventMetas.deleteWhere { MatchEventMetas.id eq id } > 0

    fun deleteByTimelineId(timelineId: Long): Int =
        MatchEventMetas.deleteWhere { MatchEventMetas.matchEventId eq timelineId }

    private fun ResultRow.toData(): MatchEventMetaData = MatchEventMetaData(
        id = this[MatchEventMetas.id].value,
        timelineId = this[MatchEventMetas.matchEventId].value,
        key = this[MatchEventMetas.key],
        value = this[MatchEventMetas.value]
    )
}
