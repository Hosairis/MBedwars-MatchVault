package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.ScrollingGui
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.util.MessageHelper
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag

object CommonGuiItems {

    val borderItem = ItemBuilder
        .from(Helper.get().getMaterialByName(Config.values.guiBorderItemType) ?: Material.GLASS)
        .name(Component.text(""))
        .flags(*ItemFlag.entries.toTypedArray())
        .asGuiItem()

    fun previousItem(gui: ScrollingGui): GuiItem =
        ItemBuilder
            .from(Helper.get().getMaterialByName(Config.values.matchGuiPrevItemType) ?: Material.ARROW)
            .name(Component.text(MessageHelper.colorize(Config.values.matchGuiPrevItemTitle)))
            .flags(*ItemFlag.entries.toTypedArray())
            .glow()
            .asGuiItem { _ -> gui.previous() }

    fun nextItem(gui: ScrollingGui): GuiItem =
        ItemBuilder
            .from(Helper.get().getMaterialByName(Config.values.matchGuiNextItemType) ?: Material.ARROW)
            .name(Component.text(MessageHelper.colorize(Config.values.matchGuiNextItemTitle)))
            .flags(*ItemFlag.entries.toTypedArray())
            .glow()
            .asGuiItem { _ -> gui.next() }

    fun backItem(action: () -> Unit): GuiItem = ItemBuilder
        .from(Helper.get().getMaterialByName(Config.values.guiBackItemType) ?: Material.BARRIER)
        .name(Component.text(MessageHelper.colorize(Config.values.guiBackItemTitle)))
        .flags(*ItemFlag.entries.toTypedArray())
        .asGuiItem { e ->
            e.isCancelled = true
            action()
        }
}