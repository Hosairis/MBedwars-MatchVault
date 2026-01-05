package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.MatchEventData
import me.hosairis.matchvault.storage.database.tables.MatchEvents
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import me.hosairis.matchvault.storage.database.tables.Players
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchEventRepository {

    fun create(data: MatchEventData): Long {
        return MatchEvents.insertAndGetId { st ->
            st[matchId] = EntityID(data.matchId, Matches)
            st[playerId] = data.playerId?.let { EntityID(it, Players) }
            st[targetId] = data.targetId?.let { EntityID(it, Players) }
            st[teamId] = data.teamId?.let { EntityID(it, MatchTeams) }
            st[timestamp] = data.timestamp
            st[type] = data.type
        }.value
    }

    fun update(data: MatchEventData): Boolean {
        return MatchEvents.update({ MatchEvents.id eq data.id }) { st ->
            st[playerId] = data.playerId?.let { EntityID(it, Players) }
            st[targetId] = data.targetId?.let { EntityID(it, Players) }
            st[teamId] = data.teamId?.let { EntityID(it, MatchTeams) }
            st[timestamp] = data.timestamp
            st[type] = data.type
        } > 0
    }

    fun delete(id: Long): Boolean {
        return MatchEvents.deleteWhere { MatchEvents.id eq id } > 0
    }

    fun read(id: Long, forUpdate: Boolean = false): MatchEventData? {
        return MatchEvents
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchEvents.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchId(matchId: Long, forUpdate: Boolean = false): List<MatchEventData> {
        return MatchEvents
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchEvents.matchId eq matchId }
            .orderBy(MatchEvents.timestamp to SortOrder.ASC)
            .map { it.toData() }
    }
    fun readByPlayerId(playerId: Long, forUpdate: Boolean = false): List<MatchEventData> {
        return MatchEvents
            .selectAll()
            .withForUpdate(forUpdate)
            .where { MatchEvents.playerId eq playerId }
            .orderBy(MatchEvents.timestamp to SortOrder.DESC)
            .map { it.toData() }
    }

    private fun ResultRow.toData(): MatchEventData {
        return MatchEventData(
            id = this[MatchEvents.id].value,
            matchId = this[MatchEvents.matchId].value,
            playerId = this[MatchEvents.playerId]?.value,
            targetId = this[MatchEvents.targetId]?.value,
            teamId = this[MatchEvents.teamId]?.value,
            timestamp = this[MatchEvents.timestamp],
            type = this[MatchEvents.type]
        )
    }

    private fun Query.withForUpdate(forUpdate: Boolean): Query {
        return if (forUpdate) this.forUpdate() else this
    }
}
