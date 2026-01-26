package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.ScrollingGui
import me.hosairis.matchvault.storage.database.enums.MatchStatus
import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.MessageHelper
import me.hosairis.matchvault.util.TimeUtil
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player

object MatchListGui {

    fun open(player: Player, target: String? = null) {
        val gui = createGui()
        val name = target ?: player.name

        gui.open(player)

        CoroutineHelper.runAsync {
            val matchList = MatchService.readMatchesOfPlayer(name)

            CoroutineHelper.runSync {
                for ((matchData, won) in matchList) {
                    gui.addItem(createMatchItem(matchData, won) { TeamListGui.open(player, matchData.id!!, target) })
                }

                gui.update()
            }
        }
    }

    private fun createGui(): ScrollingGui {
        val gui = Gui
            .scrolling()
            .title(Component.text(MessageHelper.colorize("MatchList")))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.filler.fillBorder(CommonGuiItems.fillerItem)

        gui.setItem(17, CommonGuiItems.previousItem(gui))
        gui.setItem(44, CommonGuiItems.nextItem(gui))

        return gui
    }

    fun createMatchItem(matchData: MatchData, won: Boolean, unit: () -> Unit): GuiItem {
        val material = if (matchData.status == MatchStatus.ONGOING) {
            Helper.get().getMaterialByName("blue_terracotta") ?: Material.ITEM_FRAME
        } else if (matchData.status == MatchStatus.ABORTED) {
            Helper.get().getMaterialByName("gray_terracotta") ?: Material.ITEM_FRAME
        } else if (won) {
            Helper.get().getMaterialByName("lime_terracotta") ?: Material.ITEM_FRAME
        } else {
            Helper.get().getMaterialByName("red_terracotta") ?: Material.ITEM_FRAME
        }

        val item = ItemBuilder
            .from(material)
            .name(Component.text(MessageHelper.colorize("&b${matchData.arenaName}")))
            .lore(
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Date:")),
                Component.text(MessageHelper.colorize("    &f${TimeUtil.formatMillis(matchData.startedAt)}")),
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Finished:")),
                Component.text(MessageHelper.colorize("    &f${if (matchData.endedAt == null) "N/A" else TimeUtil.formatMillis(matchData.endedAt)}")),
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Duration:")),
                Component.text(MessageHelper.colorize("    &f${if (matchData.duration == null) "N/A" else TimeUtil.formatDuration(matchData.duration)}")),
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Status: ${if (won) "&aWin" else "&4Lose"}")),
            )
            .build()

        return GuiItem(item) {
            unit()
        }
    }
}