package me.hosairis.matchvault.storage.database.tables

import me.hosairis.matchvault.storage.database.enums.MatchEventType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object MatchEvents : LongIdTable("match_events") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val targetId = reference("target_id", Players, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).nullable().index()
    val type = enumerationByName("type", 32, MatchEventType::class).index()
    val timestamp = long("timestamp").index()
    init { index(false, matchId, timestamp) }
}
