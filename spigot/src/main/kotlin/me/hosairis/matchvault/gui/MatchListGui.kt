package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.ScrollingGui
import me.hosairis.matchvault.storage.config.Config
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
            .title(Component.text(MessageHelper.colorize(Config.values.matchGuiTitle)))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.filler.fillBorder(CommonGuiItems.borderItem)

        gui.setItem(Config.values.matchGuiPrevItemSlot, CommonGuiItems.previousItem(gui))
        gui.setItem(Config.values.matchGuiNextItemSlot, CommonGuiItems.nextItem(gui))

        return gui
    }

    fun createMatchItem(matchData: MatchData, won: Boolean, unit: () -> Unit): GuiItem {
        val material = if (matchData.status == MatchStatus.ONGOING) {
            Helper.get().getMaterialByName(Config.values.matchGuiItemTypeOnGoing) ?: Material.ITEM_FRAME
        } else if (matchData.status == MatchStatus.ABORTED) {
            Helper.get().getMaterialByName(Config.values.matchGuiItemTypeAborted) ?: Material.ITEM_FRAME
        } else if (won) {
            Helper.get().getMaterialByName(Config.values.matchGuiItemTypeWon) ?: Material.ITEM_FRAME
        } else {
            Helper.get().getMaterialByName(Config.values.matchGuiItemTypeLost) ?: Material.ITEM_FRAME
        }

        val date = TimeUtil.formatMillis(matchData.startedAt)
        val finish = if (matchData.endedAt == null) {
            Config.values.matchGuiFinishNull
        } else {
            TimeUtil.formatMillis(matchData.endedAt)
        }
        val duration = if (matchData.duration == null) {
            Config.values.matchGuiDurationNull
        } else {
            TimeUtil.formatDuration(matchData.duration)
        }
        val status = if (matchData.status == MatchStatus.ABORTED) {
            Config.values.matchGuiStatusAborted
        } else if (won) {
            Config.values.matchGuiStatusWon
        } else {
            Config.values.matchGuiStatusLost
        }

        val item = ItemBuilder
            .from(material)
            .name(Component.text(MessageHelper.colorize(Config.values.matchGuiItemName.replace("%arena_name", matchData.arenaName))))
            .lore(
                Config.values.matchGuiItemLore.map {
                    Component.text(
                        MessageHelper.colorize(
                            it
                                .replace("%date", date)
                                .replace("%finish", finish)
                                .replace("%duration", duration)
                                .replace("%status", status)
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