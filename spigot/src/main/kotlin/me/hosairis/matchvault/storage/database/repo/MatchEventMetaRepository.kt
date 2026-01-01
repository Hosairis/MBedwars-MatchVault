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
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchEventMetaRepository {

    fun create(data: MatchEventMetaData): Long {
        return MatchEventMetas.insertAndGetId {
            it[matchEventId] = EntityID(data.matchEventId, MatchEvents)
            it[key] = data.key
            it[value] = data.value
        }.value
    }

    fun update(data: MatchEventMetaData): Boolean {
        val exists = MatchEventMetas
            .select(MatchEventMetas.id)
            .where { MatchEventMetas.id eq data.id }
            .forUpdate()
            .limit(1)
            .any()

        if (!exists) return false

        return MatchEventMetas.update({ MatchEventMetas.id eq data.id }) { st ->
            st[key] = data.key
            st[value] = data.value
        } > 0
    }

    fun delete(id: Long): Boolean {
        return MatchEventMetas.deleteWhere { MatchEventMetas.id eq id } > 0
    }

    fun read(id: Long): MatchEventMetaData? {
        return MatchEventMetas
            .selectAll()
            .where { MatchEventMetas.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchEventId(matchEventId: Long): List<MatchEventMetaData> {
        return MatchEventMetas
            .selectAll()
            .where { MatchEventMetas.matchEventId eq matchEventId }
            .orderBy(MatchEventMetas.id to SortOrder.ASC)
            .map { it.toData() }
    }

    private fun ResultRow.toData(): MatchEventMetaData {
        return MatchEventMetaData(
            id = this[MatchEventMetas.id].value,
            matchEventId = this[MatchEventMetas.matchEventId].value,
            key = this[MatchEventMetas.key],
            value = this[MatchEventMetas.value]
        )
    }
}
