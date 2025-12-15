package me.hosairis.matchvault.storage.database.model

data class UpgradePurchaseData(
    val id: Long? = null,
    val matchId: Long,
    val playerId: Long,
    val teamId: Long,
    val upgrade: String,
    val level: Int
)
