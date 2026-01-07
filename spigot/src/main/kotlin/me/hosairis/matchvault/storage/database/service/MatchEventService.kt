package me.hosairis.matchvault.storage.database.service

import me.hosairis.matchvault.storage.database.model.MatchEventData
import me.hosairis.matchvault.storage.database.model.MatchEventMetaData
import me.hosairis.matchvault.storage.database.repo.MatchEventMetaRepository
import me.hosairis.matchvault.storage.database.repo.MatchEventRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object MatchEventService {
    private val matchEventRepo = MatchEventRepository()
    private val metaRepo = MatchEventMetaRepository()

    /**
     * Create a timeline event and optional metas in ONE transaction.
     * Returns created timelineId.
     */
    fun createEvent(event: MatchEventData, metas: List<MatchEventMetaData> = emptyList()): Long = transaction {
        val timelineId = matchEventRepo.create(event.copy(id = null))
        metas.forEach { m ->
            metaRepo.create(m.copy(id = null, matchEventId = timelineId))
        }
        timelineId
    }

    fun readEvent(id: Long): MatchEventData? = transaction {
        matchEventRepo.read(id)
    }

    fun readEventsByMatchId(matchId: Long): List<MatchEventData> = transaction {
        matchEventRepo.readByMatchId(matchId)
    }

    fun readMetasByTimelineId(timelineId: Long): List<MatchEventMetaData> = transaction {
        metaRepo.readByMatchEventId(timelineId)
    }

    fun deleteEvent(id: Long): Boolean = transaction {
        matchEventRepo.delete(id)
    }
}
