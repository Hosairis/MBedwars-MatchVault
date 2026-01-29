package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.ScrollingGui
import me.hosairis.matchvault.util.MessageHelper
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag

object CommonGuiItems {

    val fillerItem = ItemBuilder
        .from(Helper.get().getMaterialByName("black_stained_glass_pane") ?: Material.BONE)
        .name(Component.text(""))
        .flags(*ItemFlag.entries.toTypedArray())
        .asGuiItem()

    fun previousItem(gui: ScrollingGui): GuiItem =
        ItemBuilder
            .from(Helper.get().getMaterialByName("arrow") ?: Material.ARROW)
            .name(Component.text(MessageHelper.colorize("&4Previous")))
            .flags(*ItemFlag.entries.toTypedArray())
            .glow()
            .asGuiItem { _ -> gui.previous() }

    fun nextItem(gui: ScrollingGui): GuiItem =
        ItemBuilder
            .from(Helper.get().getMaterialByName("arrow") ?: Material.ARROW)
            .name(Component.text(MessageHelper.colorize("&aNext")))
            .flags(*ItemFlag.entries.toTypedArray())
            .glow()
            .asGuiItem { _ -> gui.next() }

    fun backItem(action: () -> Unit): GuiItem = ItemBuilder
        .from(Material.BARRIER)
        .name(Component.text(MessageHelper.colorize("&4Back")))
        .flags(*ItemFlag.entries.toTypedArray())
        .asGuiItem { e ->
            e.isCancelled = true
            action()
        }
}