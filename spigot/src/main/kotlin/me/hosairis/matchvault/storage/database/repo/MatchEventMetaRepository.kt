package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.MatchEventMetaData
import me.hosairis.matchvault.storage.database.tables.MatchEventMetas
import me.hosairis.matchvault.storage.database.tables.MatchEvents
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
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
        return MatchEventMetas.update({ MatchEventMetas.id eq data.id }) { st ->
            st[key] = data.key
            st[value] = data.value
        } > 0
    }

    fun delete(id: Long): Boolean {
        return MatchEventMetas.deleteWhere { MatchEventMetas.id eq id } > 0
    }

    fun read(id: Long, forUpdate: Boolean = false): MatchEventMetaData? {
        return MatchEventMetas
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchEventMetas.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchEventId(matchEventId: Long, forUpdate: Boolean = false): List<MatchEventMetaData> {
        return MatchEventMetas
            .selectAll()
            .withForUpdate(forUpdate)
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

    private fun Query.withForUpdate(forUpdate: Boolean): Query {
        return if (forUpdate) this.forUpdate() else this
    }
}
