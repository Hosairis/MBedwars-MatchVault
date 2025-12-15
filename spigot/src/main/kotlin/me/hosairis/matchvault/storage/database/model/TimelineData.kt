package me.hosairis.matchvault.storage.database.model

import me.hosairis.matchvault.storage.database.EventType

data class TimelineData(
    val id: Long? = null,
    val matchId: Long,
    val playerId: Long? = null,
    val targetId: Long? = null,
    val teamId: Long? = null,
    val timestamp: Long,
    val type: EventType
)