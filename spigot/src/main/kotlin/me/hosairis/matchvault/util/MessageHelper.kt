package me.hosairis.matchvault.util

import me.hosairis.matchvault.MatchVault
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object MessageHelper {

    private val colorMap = mapOf(
        "YELLOW" to "&e",
        "ORANGE" to "&6",
        "RED" to "&c",
        "BLUE" to "&9",
        "LIGHT_BLUE" to "&b",
        "CYAN" to "&3",
        "GREEN" to "&a",
        "DARK_GREEN" to "&2",
        "PURPLE" to "&5",
        "PINK" to "&d",
        "WHITE" to "&f",
        "GRAY" to "&7",
        "DARK_GRAY" to "&8",
        "BROWN" to "&4",
        "BLACK" to "&0",
    )

    fun getColorCode(colorName: String): String {
        return colorMap[colorName]!!
    }

    fun colorize(input: String): String {
        return ChatColor.translateAlternateColorCodes('&', input)
    }

    fun sendMessage(player: Player, message: String) {
        player.sendMessage(colorize(message))
    }

    fun sendMessage(player: CommandSender, message: String) {
        player.sendMessage(colorize(message))
    }

    fun printSplashScreen() {
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        Log.info("${MatchVault.instance.description.name} v${MatchVault.instance.description.version} Loaded")
        Log.info("Developed by Hosairis (Dreamers Studios)")
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
    }
}