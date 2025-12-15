package me.hosairis.matchvault.storage.database.model

import java.util.UUID

data class PlayerData(
    val id: Long? = null,
    val name: String,
    val uuid: UUID,
    val firstSeen: Long,
    val lastSeen: Long
)
