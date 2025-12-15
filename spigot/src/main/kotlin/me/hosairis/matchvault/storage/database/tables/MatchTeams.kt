package me.hosairis.matchvault.storage.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object MatchTeams : LongIdTable("match_teams") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val team = varchar("team", 16).index()
    val bedDestroyedAt = long("bed_destroyed_at").nullable().index()
    val eliminatedAt = long("eliminated_at").nullable().index()
    val finalPlacement = integer("final_placement").nullable().index()
    init { uniqueIndex(matchId, team) } // enforce one team per color per match
}