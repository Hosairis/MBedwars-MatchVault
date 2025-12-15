package me.hosairis.matchvault.storage.database.service

import me.hosairis.matchvault.storage.database.model.TimelineData
import me.hosairis.matchvault.storage.database.model.TimelineMetaData
import me.hosairis.matchvault.storage.database.repo.TimelineMetaRepository
import me.hosairis.matchvault.storage.database.repo.TimelineRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class TimelineService(
    private val timelineRepo: TimelineRepository,
    private val metaRepo: TimelineMetaRepository
) {

    /**
     * Create a timeline event and optional metas in ONE transaction.
     * Returns created timelineId.
     */
    fun createEvent(event: TimelineData, metas: List<TimelineMetaData> = emptyList()): Long = transaction {
        val timelineId = timelineRepo.create(event.copy(id = null))
        metas.forEach { m ->
            metaRepo.create(m.copy(id = null, timelineId = timelineId))
        }
        timelineId
    }

    fun readEvent(id: Long): TimelineData? = transaction {
        timelineRepo.read(id)
    }

    fun readEventsByMatchId(matchId: Long): List<TimelineData> = transaction {
        timelineRepo.readByMatchId(matchId)
    }

    fun readMetasByTimelineId(timelineId: Long): List<TimelineMetaData> = transaction {
        metaRepo.readByTimelineId(timelineId)
    }

    fun deleteEvent(id: Long): Boolean = transaction {
        timelineRepo.delete(id)
    }
}
