package me.hosairis.matchvault.command

import me.hosairis.matchvault.gui.MatchListGui
import me.hosairis.matchvault.storage.config.Messages
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.MessageHelper
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MatchHistoryCMD: CommandExecutor {
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

        val isSelf = args.isEmpty()
        val target = if (args.isEmpty()) player.name else args[0]
        val permission = if (args.isEmpty()) "mva.commands.history" else "mva.commands.history.others"

        if (!player.hasPermission(permission)) {
            MessageHelper.sendMessage(
                player,
                Messages.values.insufficientPermissions.replace("%permission", permission)
            )
            return true
        }

        CoroutineHelper.runAsync {
            val matchList = MatchService.readMatchesOfPlayer(target)

            CoroutineHelper.runSync {
                if (!player.isOnline) return@runSync

                if (matchList.isEmpty()) {
                    val message = if (isSelf) {
                        Messages.values.noRecordedMatches
                    } else {
                        Messages.values.noRecordedMatchesOthers.replace("%player", target)
                    }
                    MessageHelper.sendMessage(player, message)
                    return@runSync
                }

                if (isSelf) {
                    MatchListGui.open(player)
                } else {
                    MatchListGui.open(player, target)
                }
            }
        }

        return true
    }
}