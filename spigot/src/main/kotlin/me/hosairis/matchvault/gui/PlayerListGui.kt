package me.hosairis.matchvault.gui

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.storage.database.service.PlayerService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.MessageHelper
import me.hosairis.matchvault.util.TimeUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerListGui {

    fun open(player: Player, matchId: Long, teamData: MatchTeamData, target: String? = null) {
        val gui = createGui()
        val name = target ?: player.name
        gui.setItem(Config.values.playerGuiBackItemSlot, CommonGuiItems.backItem { TeamListGui.open(player, matchId, target) })

        gui.open(player)

        CoroutineHelper.runAsync {
            val matchPlayerList = MatchService.readPlayersOfTeam(teamData.id!!)
            val playerId = PlayerService.readIdByName(name) ?: -1

            CoroutineHelper.runSync {
                for (matchPlayerData in matchPlayerList) {
                    gui.addItem(createPlayerItem(matchPlayerData, teamData.team, matchPlayerData.playerId == playerId))
                }
                gui.update()
            }
        }
    }

    private fun createGui(): Gui {
        val gui = Gui
            .gui()
            .title(Component.text(MessageHelper.colorize(Config.values.playerGuiTitle)))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.filler.fillBorder(CommonGuiItems.borderItem)

        return gui
    }

    fun createPlayerItem(matchPlayerData: MatchPlayerData, team: String, self: Boolean): GuiItem {
        val uuid = PlayerService.readUuidById(matchPlayerData.playerId)
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        val status = if (matchPlayerData.won) Config.values.playerGuiStatusWin else Config.values.playerGuiStatusLose

        return ItemBuilder
            .skull()
            .owner(offlinePlayer)
            .name(Component.text(MessageHelper.colorize(
                Config.values.playerGuiItemName
                    .replace("%team_color", MessageHelper.getColorCode(team))
                    .replace("%player_name", offlinePlayer.name)
                    .replace("%own", if (self) Config.values.playerGuiOwnItem else "")
            )))
            .lore(
                Config.values.playerGuiItemLore.map {
                    Component.text(
                        MessageHelper.colorize(
                            it
                                .replace("%kills", "${matchPlayerData.kills}")
                                .replace("%final_kills", "${matchPlayerData.finalKills}")
                                .replace("%top_kill_streak", "${matchPlayerData.topKillStreak}")
                                .replace("%deaths", "${matchPlayerData.deaths}")
                                .replace("%beds_destroyed", "${matchPlayerData.bedsDestroyed}")
                                .replace("%resource_iron_spawner", "${matchPlayerData.resIronSpawner}")
                                .replace("%resource_gold_spawner", "${matchPlayerData.resGoldSpawner}")
                                .replace("%resource_diamond_spawner", "${matchPlayerData.resDiamondSpawner}")
                                .replace("%resource_emerald_spawner", "${matchPlayerData.resEmeraldSpawner}")
                                .replace("%resource_iron", "${matchPlayerData.resIron}")
                                .replace("%resource_gold", "${matchPlayerData.resGold}")
                                .replace("%resource_diamond", "${matchPlayerData.resDiamond}")
                                .replace("%resource_emerald", "${matchPlayerData.resEmerald}")
                                .replace("%play_time", TimeUtil.formatDuration(matchPlayerData.playTime))
                                .replace("%status", status)
                        )
                    )
                }
            )
            .asGuiItem()
    }
}