package me.hosairis.matchvault.command

import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.gui.MatchListGui
import me.hosairis.matchvault.storage.config.Messages
import me.hosairis.matchvault.util.MessageHelper
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
        val player = sender as? Player ?: run {
            MessageHelper.sendMessage(sender, Messages.values.consoleProhibitedCommand)
            return true
        }

        if (args.isEmpty()) {
            if (!player.hasPermission("mva.commands.history")) {
                MessageHelper.sendMessage(
                    player,
                    Messages.values.insufficientPermissions.replace("%permission", "mva.commands.history")
                )
                return true
            }

            MatchListGui.open(player)
            return true
        }

        if (!player.hasPermission("mva.commands.history.others")) {
            MessageHelper.sendMessage(
                player,
                Messages.values.insufficientPermissions.replace("%permission", "mva.commands.history.others")
            )
            return true
        }

        MatchListGui.open(player, args[0])
        return true
    }
}