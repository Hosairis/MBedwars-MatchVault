package me.hosairis.matchvault.storage.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object MatchPlayers : LongIdTable("match_players") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.CASCADE).index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).index()
    val kills = integer("kills").default(0)
    val finalKills = integer("final_kills").default(0)
    val topKillStreak = integer("top_kill_streak").default(0)
    val deaths = integer("deaths").default(0)
    val bedsDestroyed = integer("beds_destroyed").default(0)
    val playTime = long("play_time").default(0)
    val resIron = long("res_iron").default(0)
    val resGold = long("res_gold").default(0)
    val resDiamond = long("res_diamond").default(0)
    val resEmerald = long("res_emerald").default(0)
    val resIronSpawner = long("res_iron_sp").default(0)
    val resGoldSpawner = long("res_gold_sp").default(0)
    val resDiamondSpawner = long("res_diamond_sp").default(0)
    val resEmeraldSpawner = long("res_emerald_sp").default(0)
    val won = bool("won").default(false)
    init { uniqueIndex(matchId, playerId) } // enforce one player entry per match
}