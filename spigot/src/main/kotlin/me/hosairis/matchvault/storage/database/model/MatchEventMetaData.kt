package me.hosairis.matchvault.storage.database.model

data class MatchEventMetaData(
    val id: Long? = null,
    val matchEventId: Long,
    val key: String,
    val value: String
)
