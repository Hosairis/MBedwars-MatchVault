package me.hosairis.matchvault.storage.config

import de.marcely.bedwars.tools.Helper
import dev.dejvokep.boostedyaml.YamlDocument
import org.bukkit.Material
import kotlin.String

object Config : AbstractConfig("config.yml") {

    data class Values(
        val allowedMaterials: List<Material>,
        val allowedStats: List<String>,

        val serverName: String,

        val databaseType: String,
        val databaseHost: String,
        val databasePort: Int,
        val databaseUser: String,
        val databasePassword: String,
        val databaseName: String,
        val databaseParameters: String,

        val dateFormat: String,
        val durationIncludeMillis: Boolean,
        val durationShortFormat: Boolean,

        val guiBorderItemType: String,
        val guiBackItemType: String,
        val guiBackItemTitle: String,

        val matchGuiTitle: String,
        val matchGuiPrevItemType: String,
        val matchGuiPrevItemTitle: String,
        val matchGuiPrevItemSlot: Int,
        val matchGuiNextItemType: String,
        val matchGuiNextItemTitle: String,
        val matchGuiNextItemSlot: Int,
        val matchGuiFinishNull: String,
        val matchGuiDurationNull: String,
        val matchGuiStatusWon: String,
        val matchGuiStatusLost: String,
        val matchGuiStatusAborted: String,
        val matchGuiItemName: String,
        val matchGuiItemLore: List<String>,
        val matchGuiItemTypeWon: String,
        val matchGuiItemTypeLost: String,
        val matchGuiItemTypeOnGoing: String,
        val matchGuiItemTypeAborted: String,

        val teamGuiTitle: String,
        val teamGuiBackItemSlot: Int,
        val teamGuiBedDestructionNull: String,
        val teamGuiEliminationNull: String,
        val teamGuiFinalPlacementNull: String,
        val teamGuiItemName: String,
        val teamGuiItemGlow: Boolean,
        val teamGuiItemLore: List<String>,

        val playerGuiTitle: String,
        val playerGuiBackItemSlot: Int,
        val playerGuiStatusWin: String,
        val playerGuiStatusLose: String,
        val playerGuiOwnItem: String,
        val playerGuiItemName: String,
        val playerGuiItemLore: List<String>,

        val configVersion: Int
    )

    @Volatile
    var values: Values = Values(
        allowedMaterials = listOf(
            Helper.get().getMaterialByName("iron_ingot") ?: Material.IRON_INGOT,
            Helper.get().getMaterialByName("gold_ingot") ?: Material.GOLD_INGOT,
            Helper.get().getMaterialByName("diamond") ?: Material.DIAMOND,
            Helper.get().getMaterialByName("emerald") ?: Material.EMERALD
        ),
        allowedStats = listOf(
            "bedwars:kills",
            "bedwars:final_kills",
            "bedwars:deaths",
            "bedwars:beds_destroyed",
            "bedwars:top_kill_streak",
            "bedwars:play_time"
        ),

        serverName = "unknown",

        databaseType = "H2",
        databaseHost = "localhost",
        databasePort = 3306,
        databaseUser = "root",
        databasePassword = "Admin@123",
        databaseName = "matchvault",
        databaseParameters = "?useSSL=false&allowMultiQueries=true",

        dateFormat = "yyyy/MM/dd - hh:mm a",
        durationIncludeMillis = false,
        durationShortFormat = true,

        guiBorderItemType = "black_stained_glass_pane",
        guiBackItemType = "barrier",
        guiBackItemTitle = "&4Back",

        matchGuiTitle = "&bMatchList",
        matchGuiPrevItemType = "arrow",
        matchGuiPrevItemTitle = "&4Previous",
        matchGuiPrevItemSlot = 17,
        matchGuiNextItemType = "arrow",
        matchGuiNextItemTitle = "&aNext",
        matchGuiNextItemSlot = 44,
        matchGuiFinishNull = "N/A",
        matchGuiDurationNull = "N/A",
        matchGuiStatusWon = "&aWin",
        matchGuiStatusLost = "&4Lose",
        matchGuiStatusAborted = "&6Aborted",
        matchGuiItemName = "&b%arena_name",
        matchGuiItemLore = listOf(),
        matchGuiItemTypeWon = "lime_terracotta",
        matchGuiItemTypeLost = "red_terracotta",
        matchGuiItemTypeOnGoing = "blue_terracotta",
        matchGuiItemTypeAborted = "yellow_terracotta",

        teamGuiTitle = "&bTeam List",
        teamGuiBackItemSlot = 0,
        teamGuiBedDestructionNull = "Intact",
        teamGuiEliminationNull = "N/A",
        teamGuiFinalPlacementNull = "N/A",
        teamGuiItemName = "%team_color%team_name",
        teamGuiItemGlow = true,
        teamGuiItemLore = listOf(),

        playerGuiTitle = "&bTeam List",
        playerGuiBackItemSlot = 0,
        playerGuiStatusWin = "&aWinner",
        playerGuiStatusLose = "&4Loser",
        playerGuiOwnItem = "&7(YOU)",
        playerGuiItemName = "%player_name %own",
        playerGuiItemLore = listOf(),

        configVersion =  1
    )
        private set

    override fun loadValues(doc: YamlDocument) {
        values = Values(
            allowedMaterials = listOf(
                Helper.get().getMaterialByName("iron_ingot") ?: Material.IRON_INGOT,
                Helper.get().getMaterialByName("gold_ingot") ?: Material.GOLD_INGOT,
                Helper.get().getMaterialByName("diamond") ?: Material.DIAMOND,
                Helper.get().getMaterialByName("emerald") ?: Material.EMERALD
            ),
            allowedStats = listOf(
                "bedwars:kills",
                "bedwars:final_kills",
                "bedwars:deaths",
                "bedwars:beds_destroyed",
                "bedwars:top_kill_streak",
                "bedwars:play_time"
            ),

            serverName = doc.getString("server-name"),

            databaseType = doc.getString("database.type"),
            databaseHost = doc.getString("database.host"),
            databasePort = doc.getInt("database.port"),
            databaseUser = doc.getString("database.user"),
            databasePassword = doc.getString("database.password"),
            databaseName = doc.getString("database.name"),
            databaseParameters = doc.getString("database.parameters"),

            dateFormat = doc.getString("date.format"),
            durationIncludeMillis = doc.getBoolean("duration.include-millisecond"),
            durationShortFormat = doc.getBoolean("duration.short-format"),

            guiBorderItemType = doc.getString("gui.global.border-item.type"),
            guiBackItemType = doc.getString("gui.global.back-item.type"),
            guiBackItemTitle = doc.getString("gui.global.back-item.title"),

            matchGuiTitle = doc.getString("gui.match-list.title"),
            matchGuiPrevItemType = doc.getString("gui.match-list.previous-item.type"),
            matchGuiPrevItemTitle = doc.getString("gui.match-list.previous-item.title"),
            matchGuiPrevItemSlot = doc.getInt("gui.match-list.previous-item.slot"),
            matchGuiNextItemType = doc.getString("gui.match-list.next-item.type"),
            matchGuiNextItemTitle = doc.getString("gui.match-list.next-item.title"),
            matchGuiNextItemSlot = doc.getInt("gui.match-list.next-item.slot"),
            matchGuiFinishNull = doc.getString("gui.match-list.placeholders.finish.if-null"),
            matchGuiDurationNull = doc.getString("gui.match-list.placeholders.duration.if-null"),
            matchGuiStatusWon = doc.getString("gui.match-list.placeholders.status.if-won"),
            matchGuiStatusLost = doc.getString("gui.match-list.placeholders.status.if-lost"),
            matchGuiStatusAborted = doc.getString("gui.match-list.placeholders.status.if-aborted"),
            matchGuiItemName = doc.getString("gui.match-list.match-item.name"),
            matchGuiItemLore = doc.getStringList("gui.match-list.match-item.lore"),
            matchGuiItemTypeWon = doc.getString("gui.match-list.match-item.type.won"),
            matchGuiItemTypeLost = doc.getString("gui.match-list.match-item.type.lost"),
            matchGuiItemTypeOnGoing = doc.getString("gui.match-list.match-item.type.on-going"),
            matchGuiItemTypeAborted = doc.getString("gui.match-list.match-item.type.aborted"),

            teamGuiTitle = doc.getString("gui.team-list.title"),
            teamGuiBackItemSlot = doc.getInt("gui.team-list.back-item-slot"),
            teamGuiBedDestructionNull = doc.getString("gui.team-list.placeholders.bed-destruction.if-null"),
            teamGuiEliminationNull = doc.getString("gui.team-list.placeholders.elimination.if-null"),
            teamGuiFinalPlacementNull = doc.getString("gui.team-list.placeholders.final-placement.if-null"),
            teamGuiItemName = doc.getString("gui.team-list.match-item.name"),
            teamGuiItemGlow = doc.getBoolean("gui.team-list.match-item.glow-own"),
            teamGuiItemLore = doc.getStringList("gui.team-list.match-item.lore"),

            playerGuiTitle = doc.getString("gui.player-list.title"),
            playerGuiBackItemSlot = doc.getInt("gui.player-list.back-item-slot"),
            playerGuiStatusWin = doc.getString("gui.player-list.placeholders.status.if-win"),
            playerGuiStatusLose = doc.getString("gui.player-list.placeholders.status.if-lose"),
            playerGuiOwnItem = doc.getString("gui.player-list.placeholders.own-item"),
            playerGuiItemName = doc.getString("gui.player-list.match-item.name"),
            playerGuiItemLore = doc.getStringList("gui.player-list.match-item.lore"),

            configVersion = doc.getInt("config-version")
        )
    }
}
