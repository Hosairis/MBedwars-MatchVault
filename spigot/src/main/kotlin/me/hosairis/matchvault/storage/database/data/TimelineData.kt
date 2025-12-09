package me.hosairis.matchvault.storage.database.data

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
        fun read(id: Long): TimelineData? {
            return Timelines
                .selectAll()
                .where { Timelines.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        fun readByMatchId(matchId: Long): List<TimelineData> {
            val matchRef = EntityID(matchId, Matches)
            return Timelines
                .selectAll()
                .where { Timelines.matchId eq matchRef }
                .map { it.toData() }
        }

        fun readByPlayerId(playerId: Long): List<TimelineData> {
            val playerRef = EntityID(playerId, Players)
            return Timelines
                .selectAll()
                .where { Timelines.playerId eq playerRef }
                .map { it.toData() }
        }

        fun readByTargetId(targetId: Long): List<TimelineData> {
            val targetRef = EntityID(targetId, Players)
            return Timelines
                .selectAll()
                .where { Timelines.targetId eq targetRef }
                .map { it.toData() }
        }

        fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return Timelines.update({ Timelines.id eq id }) { statement ->
                val fetchRow = {
                    Timelines
                        .selectAll()
                        .where { Timelines.id eq id }
                        .forUpdate()
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
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

    fun create(): Boolean {
        val newId = Timelines.insertAndGetId { statement ->
            statement[Timelines.matchId] = EntityID(this@TimelineData.matchId, Matches)
            statement[Timelines.playerId] = this@TimelineData.playerId?.let { EntityID(it, Players) }
            statement[Timelines.targetId] = this@TimelineData.targetId?.let { EntityID(it, Players) }
            statement[Timelines.teamId] = this@TimelineData.teamId?.let { EntityID(it, MatchTeams) }
            statement[Timelines.timestamp] = this@TimelineData.timestamp
            statement[Timelines.type] = this@TimelineData.type
        }
        this@TimelineData.id = newId.value
        return true
    }

    fun update(builder: (UpdateBuilder<Int>.(TimelineData) -> Unit)? = null): Boolean {
        val recordId = id ?: return false
        return Timelines.update({ Timelines.id eq recordId }) { statement ->
            if (builder == null) {
                statement[Timelines.timestamp] = this@TimelineData.timestamp
                statement[Timelines.type] = this@TimelineData.type
            } else {
                builder.invoke(statement, this@TimelineData)
            }
        } > 0
    }

    fun delete(): Boolean {
        val recordId = id ?: return false
        return Timelines.deleteWhere { Timelines.id eq recordId } > 0
    }

    fun loadMetas() {
        val timelineId = id ?: return
        metas.clear()
        metas.addAll(TimelineMetaData.readByTimelineId(timelineId))
    }
}
