package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.util.MessageHelper
import me.hosairis.matchvault.util.TimeUtil
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

object TeamListGui {

    fun open(player: Player, matchId: Long, target: String? = null) {
        val gui = createGui()
        val name = target ?: player.name
        gui.setItem(0, CommonGuiItems.backItem { MatchListGui.open(player, target) })

        gui.open(player)

        CoroutineHelper.runAsync {
            //TODO: sort by placement
            val teamDataList = MatchService.readTeamsOfMatch(matchId)
            val playerTeamId = MatchService.readTeamOfPlayer(matchId, name)

            CoroutineHelper.runSync {
                teamDataList.forEach { teamData ->
                    gui.addItem(createTeamItem(teamData, teamData.id == playerTeamId?.id) { PlayerListGui.open(player, teamData.id!!, target) })
                }
                gui.update()
            }
        }
    }

    private fun createGui(): Gui {
        val gui = Gui
            .gui()
            .title(Component.text("Team List"))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.filler.fillBorder(CommonGuiItems.fillerItem)

        return gui
    }

    fun createTeamItem(teamData: MatchTeamData, ownTeam: Boolean, unit: () -> Unit): GuiItem {
        val material = Helper.get().getMaterialByName("${teamData.team}_wool") ?: Material.WOOL
        val item = ItemBuilder
            .from(material)
            .name(Component.text(MessageHelper.colorize("${ChatColor.valueOf(teamData.team)}${teamData.team}")))
            .glow(ownTeam)
            .lore(
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Bed destruction time:")),
                Component.text(MessageHelper.colorize("    &f${if (teamData.bedDestroyedAt == null) "N/A" else TimeUtil.formatMillis(teamData.bedDestroyedAt)}")),
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Elimination time:")),
                Component.text(MessageHelper.colorize("    &f${if (teamData.eliminatedAt == null) "N/A" else TimeUtil.formatMillis(teamData.eliminatedAt)}")),
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Final Placement: &f${teamData.finalPlacement ?: "N/A"}"))
            )
            .build()

        return GuiItem(item) {
            unit()
        }
    }
}