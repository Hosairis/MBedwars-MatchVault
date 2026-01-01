package me.hosairis.matchvault.storage.database.model

data class MatchEventMetaData(
    val id: Long? = null,
    val timelineId: Long,
    val key: String,
    val value: String
)
