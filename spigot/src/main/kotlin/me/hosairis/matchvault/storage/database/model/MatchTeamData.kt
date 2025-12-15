package me.hosairis.matchvault.storage.database.model

data class MatchTeamData(
    val id: Long? = null,
    val matchId: Long,
    val team: String,
    val bedDestroyedAt: Long? = null,
    val eliminatedAt: Long? = null,
    val finalPlacement: Int? = null
)