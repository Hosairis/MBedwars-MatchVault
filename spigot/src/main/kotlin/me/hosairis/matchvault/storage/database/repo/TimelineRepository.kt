package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.TimelineData
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import me.hosairis.matchvault.storage.database.tables.Players
import me.hosairis.matchvault.storage.database.tables.Timelines
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class TimelineRepository {

    fun create(data: TimelineData): Long =
        Timelines.insertAndGetId { st ->
            st[matchId] = EntityID(data.matchId, Matches)
            st[playerId] = data.playerId?.let { EntityID(it, Players) }
            st[targetId] = data.targetId?.let { EntityID(it, Players) }
            st[teamId] = data.teamId?.let { EntityID(it, MatchTeams) }
            st[timestamp] = data.timestamp
            st[type] = data.type
        }.value

    fun read(id: Long): TimelineData? =
        Timelines
            .selectAll()
            .where { Timelines.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readByMatchId(matchId: Long): List<TimelineData> =
        Timelines
            .selectAll()
            .where { Timelines.matchId eq matchId }
            .orderBy(Timelines.timestamp to SortOrder.ASC)
            .map { it.toData() }

    fun readByPlayerId(playerId: Long): List<TimelineData> =
        Timelines
            .selectAll()
            .where { Timelines.playerId eq playerId }
            .orderBy(Timelines.timestamp to SortOrder.DESC)
            .map { it.toData() }

    fun update(data: TimelineData): Boolean =
        Timelines.update({ Timelines.id eq data.id }) { st ->
            st[playerId] = data.playerId?.let { EntityID(it, Players) }
            st[targetId] = data.targetId?.let { EntityID(it, Players) }
            st[teamId] = data.teamId?.let { EntityID(it, MatchTeams) }
            st[timestamp] = data.timestamp
            st[type] = data.type
        } > 0

    fun delete(id: Long): Boolean =
        Timelines.deleteWhere { Timelines.id eq id } > 0

    fun deleteByMatchId(matchId: Long): Int =
        Timelines.deleteWhere { Timelines.matchId eq matchId }

    private fun ResultRow.toData(): TimelineData = TimelineData(
        id = this[Timelines.id].value,
        matchId = this[Timelines.matchId].value,
        playerId = this[Timelines.playerId]?.value,
        targetId = this[Timelines.targetId]?.value,
        teamId = this[Timelines.teamId]?.value,
        timestamp = this[Timelines.timestamp],
        type = this[Timelines.type]
    )
}
