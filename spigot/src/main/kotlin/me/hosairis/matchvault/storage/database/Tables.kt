package me.hosairis.matchvault.storage.database

import de.marcely.bedwars.api.game.shop.ShopOpenCause
import me.hosairis.matchvault.storage.config.Config
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

enum class MatchStatus {
    ONGOING, ENDED, ABORTED
}

enum class EventType {
    MATCH_START, MATCH_END, PLAYER_KILL, PLAYER_DEATH,
    BED_BREAK, TEAM_ELIMINATED, SHOP_PURCHASE, UPGRADE_PURCHASE,
    TRAP_PURCHASE, TRAP_TRIGGERED, RESOURCE_PICKUP, RESOURCE_DROP,
    CHAT_MESSAGE, CUSTOM
}

object Players : LongIdTable("players") {
    val name = varchar("name", 64).index()
    val uuid = uuid("uuid").uniqueIndex()
    val firstSeen = long("first_seen")
    val lastSeen = long("last_seen")
}

object Matches : LongIdTable("matches") {
    val arenaName = varchar("arena_name", 64)        // arena.name from BedWars API
    val worldName = varchar("world_name", 64)        // arena.gameworld.name
    val mode = integer("mode").index()
    val startedAt = long("started_at").index()
    val endedAt = long("ended_at").nullable()
    val duration = long("duration").nullable()
    val status = enumerationByName("status", 16, MatchStatus::class).default(MatchStatus.ONGOING).index()
    val server = varchar("server", 64).default(Config.SERVER_NAME).index()
    val winnerTeam = varchar("winner_team", 16).nullable()
    val isTie = bool("is_tie").default(false)
}

object MatchTeams : LongIdTable("match_teams") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val teamId = varchar("team", 16).index()
    val bedDestroyedAt = long("bed_destroyed_at").nullable().index()
    val eliminatedAt = long("eliminated_at").nullable().index()
    val finalPlacement = integer("final_placement").nullable().index()
    init { uniqueIndex(matchId, teamId) } // enforce one team per color per match
}

object MatchPlayers : LongIdTable("match_players") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.CASCADE).index()
    val team = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).index()
    val kills = integer("kills").default(0)
    val finalKills = integer("final_kills").default(0)
    val deaths = integer("deaths").default(0)
    val bedsBroken = integer("beds_broken").default(0)
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

object ShopPurchases : LongIdTable("shop_purchases") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.CASCADE).index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).index()
    val item = varchar("item", 64)
    val amount = integer("amount").default(1)
    val openCause = enumerationByName("open_cause", 32, ShopOpenCause::class)
}

object UpgradePurchases : LongIdTable("upgrade_purchases") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val buyerId = reference("buyer_id", Players, onDelete = ReferenceOption.CASCADE).index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).index()
    val upgrade = varchar("upgrade", 64)
    val level = integer("level").default(1)
}

object PlayerStats : LongIdTable("player_stats") {
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val matchesPlayed = integer("matches_played").default(0)
    val wins = integer("wins").default(0)
    val losses = integer("losses").default(0)
    val kills = integer("kills").default(0)
    val finalKills = integer("final_kills").default(0)
    val deaths = integer("deaths").default(0)
    val bedsBroken = integer("beds_broken").default(0)
    val resIron = long("res_iron").default(0)
    val resGold = long("res_gold").default(0)
    val resDiamond = long("res_diamond").default(0)
    val resEmerald = long("res_emerald").default(0)
    val resIronSpawner = long("res_iron_sp").default(0)
    val resGoldSpawner = long("res_gold_sp").default(0)
    val resDiamondSpawner = long("res_diamond_sp").default(0)
    val resEmeraldSpawner = long("res_emerald_sp").default(0)
}

object Timelines : LongIdTable("timelines") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("actor_id", Players, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val targetId = reference("target_id", Players, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).nullable().index()
    val timestamp = long("timestamp").index()
    val type = enumerationByName("type", 32, EventType::class).index()
}

object TimelineMetas : LongIdTable("timeline_metas") {
    val timelineId = reference("timeline_id", Timelines, onDelete = ReferenceOption.CASCADE).index()
    val key = varchar("key", 64).index()
    val value = varchar("value", 255).index()
}
