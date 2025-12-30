package me.hosairis.matchvault.storage.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object TimelineMetas : LongIdTable("timeline_metas") {
    val timelineId = reference("timeline_id", Timelines, onDelete = ReferenceOption.CASCADE)
    val key = varchar("key", 64)
    val value = varchar("value", 255)
    init { index(false, timelineId, key) }
}
