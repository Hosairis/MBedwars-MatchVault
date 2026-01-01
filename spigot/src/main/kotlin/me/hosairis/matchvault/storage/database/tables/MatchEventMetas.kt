package me.hosairis.matchvault.storage.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object MatchEventMetas : LongIdTable("match_event_metas") {
    val matchEventId = reference("timeline_id", MatchEvents, onDelete = ReferenceOption.CASCADE)
    val key = varchar("key", 64)
    val value = varchar("value", 255)
    init { index(false, matchEventId, key) }
}
