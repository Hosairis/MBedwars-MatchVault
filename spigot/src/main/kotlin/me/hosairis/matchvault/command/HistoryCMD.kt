package me.hosairis.matchvault.command

import me.hosairis.matchvault.gui.MatchListGui
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HistoryCMD: CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true
        val target = if (args.isEmpty()) sender.name else args[0]
        MatchListGui.open(sender, target)
        return true
    }
}