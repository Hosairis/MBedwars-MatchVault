package me.hosairis.matchvault.storage.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object UpgradePurchases : LongIdTable("upgrade_purchases") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.CASCADE).index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).index()
    val upgrade = varchar("upgrade", 64)
    val level = integer("level").default(1)
}