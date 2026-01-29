package me.hosairis.matchvault.command

import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.config.Messages
import me.hosairis.matchvault.util.MessageHelper
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class MatchVaultCMD: CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty()) {
            MessageHelper.sendMessage(
                sender,
                "&7[&bMatchVault&7] &b${MatchVault.instance.description.name} &7version &b${MatchVault.instance.description.version} &7by &bHosairis"
            )
            return true
        }

        if (args[0].equals("reload", true) || args[0].equals("rl", true)) {
            if (!sender.hasPermission("mva.commands.reload")) {
                MessageHelper.sendMessage(
                    sender,
                    Messages.values.insufficientPermissions.replace(
                        "%permission",
                        "mva.commands.reload",
                    )
                )
                return true
            }
            try {
                Config.reload()
                Messages.reload()
                MessageHelper.sendMessage(sender, Messages.values.reloadSuccess)
            } catch (e: Exception) {
                MessageHelper.sendMessage(sender, Messages.values.reloadFailed)
                e.printStackTrace()
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            val completions = mutableListOf<String>()
            if (sender.hasPermission("mva.commands.reload")) {
                completions.add("reload")
            }
            return completions.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
        }
        return mutableListOf()
    }
}