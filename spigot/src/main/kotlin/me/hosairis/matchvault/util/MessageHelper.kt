package me.hosairis.matchvault.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object MessageHelper {
    fun colorize(input: String): String {
        return ChatColor.translateAlternateColorCodes('&', input)
    }

    fun sendMessage(player: Player, message: String) {
        player.sendMessage(colorize(message))
    }

    fun sendMessage(player: CommandSender, message: String) {
        player.sendMessage(colorize(message))
    }
}