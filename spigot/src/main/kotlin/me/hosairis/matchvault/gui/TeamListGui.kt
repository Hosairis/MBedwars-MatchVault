package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.MessageHelper
import me.hosairis.matchvault.util.TimeUtil
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

object TeamListGui {

    fun open(player: Player, matchId: Long, target: String? = null) {
        val gui = createGui()
        val name = target ?: player.name
        gui.setItem(Config.values.teamGuiBackItemSlot, CommonGuiItems.backItem { MatchListGui.open(player, target) })

        gui.open(player)

        CoroutineHelper.runAsync {
            val teamDataList = MatchService.readTeamsOfMatch(matchId)
            val playerTeamId = MatchService.readTeamIdOfPlayer(matchId, name)

            CoroutineHelper.runSync {
                teamDataList.forEach { teamData ->
                    gui.addItem(createTeamItem(teamData, teamData.id == playerTeamId) { PlayerListGui.open(player, matchId, teamData, target) })
                }
                gui.update()
            }
        }
    }

    private fun createGui(): Gui {
        val gui = Gui
            .gui()
            .title(Component.text(Config.values.teamGuiTitle))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.filler.fillBorder(CommonGuiItems.fillerItem)

        return gui
    }

    fun createTeamItem(teamData: MatchTeamData, ownTeam: Boolean, unit: () -> Unit): GuiItem {
        val material = Helper.get().getMaterialByName("${teamData.team}_wool") ?: Material.WOOL
        val bedDestroyedTime = if (teamData.bedDestroyedAt == null) {
            Config.values.teamGuiBedDestructionNull
        } else {
            TimeUtil.formatMillis(teamData.bedDestroyedAt)
        }
        val eliminationTime = if (teamData.eliminatedAt == null) {
            Config.values.teamGuiEliminationNull
        } else {
            TimeUtil.formatMillis(teamData.eliminatedAt)
        }
        val finalPlacement = teamData.finalPlacement ?: Config.values.teamGuiFinalPlacementNull

        val item = ItemBuilder
            .from(material)
            .name(Component.text(MessageHelper.colorize(
                Config.values.teamGuiItemName
                    .replace("%team_color", "${ChatColor.valueOf(teamData.team)}")
                    .replace("%team_name", teamData.team.lowercase().replaceFirstChar { it.uppercase() })
            )))
            .glow(if (Config.values.teamGuiItemGlow) ownTeam else false)
            .lore(
                Config.values.teamGuiItemLore.map {
                    Component.text(
                        MessageHelper.colorize(
                            it
                                .replace("%bed_destruction_time", bedDestroyedTime)
                                .replace("%elimination_time", eliminationTime)
                                .replace("%final_placement", "$finalPlacement")
                        )
                    )
                }
            )
            .build()

        return GuiItem(item) {
            unit()
        }
    }
}