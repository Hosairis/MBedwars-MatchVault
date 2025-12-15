package me.hosairis.matchvault.storage.database.model

data class MatchPlayerData(
    val id: Long? = null,
    val matchId: Long,
    val playerId: Long,
    val teamId: Long,
    val kills: Int = 0,
    val finalKills: Int = 0,
    val deaths: Int = 0,
    val bedsDestroyed: Int = 0,
    val resIron: Long = 0,
    val resGold: Long = 0,
    val resDiamond: Long = 0,
    val resEmerald: Long = 0,
    val resIronSpawner: Long = 0,
    val resGoldSpawner: Long = 0,
    val resDiamondSpawner: Long = 0,
    val resEmeraldSpawner: Long = 0,
    val won: Boolean = false
)
