package me.hosairis.matchvault.storage.database.tables

import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.enums.MatchStatus
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object Matches : LongIdTable("matches") {
    val arenaName = varchar("arena_name", 64)
    val mode = integer("mode").index()
    val startedAt = long("started_at").index()
    val endedAt = long("ended_at").nullable()
    val duration = long("duration").nullable()
    val status = enumerationByName("status", 16, MatchStatus::class).default(MatchStatus.ONGOING).index()
    val isTie = bool("is_tie").default(false)
    val winnerTeamId = reference("winner_team_id", MatchTeams, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val server = varchar("server", 64).default(Config.values.serverName).index()
}
