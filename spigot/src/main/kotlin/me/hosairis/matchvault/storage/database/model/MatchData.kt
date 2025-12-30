package me.hosairis.matchvault.storage.database.model

import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.enums.MatchStatus

data class MatchData(
    val id: Long? = null,
    val arenaName: String,
    val mode: Int,
    val startedAt: Long,
    val endedAt: Long? = null,
    val duration: Long? = null,
    val status: MatchStatus = MatchStatus.ONGOING,
    val isTie: Boolean = false,
    val winnerTeamId: Long? = null,
    val server: String = Config.values.serverName
)