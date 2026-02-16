package me.hosairis.matchvault.command

import de.marcely.bedwars.api.command.CommandHandler
import de.marcely.bedwars.api.command.SubCommand
import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.gui.MatchListGui
import me.hosairis.matchvault.storage.config.Messages
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.MessageHelper
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class MatchHistoryCMD : CommandHandler {

    override fun getPlugin(): Plugin = MatchVault.instance

    override fun onRegister(cmd: SubCommand) {}

    override fun onFire(
        sender: CommandSender,
        fullUsage: String,
        args: Array<out String>
    ) {
        val player = sender as? Player ?: run {
            MessageHelper.sendMessage(sender, Messages.values.consoleProhibitedCommand)
            return
        }
        val isSelf = args.isEmpty()
        val target = if (args.isEmpty()) player.name else args[0]
        val permission = if (args.isEmpty()) "matchvault.commands.history" else "matchvault.commands.history.others"

        if (!player.hasPermission(permission)) {
            MessageHelper.sendMessage(
                player,
                Messages.values.insufficientPermissions.replace("%permission", permission)
            )
            return
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
    }

    override fun onAutocomplete(
        sender: CommandSender,
        args: Array<out String>
    ): List<String> {
        if (!sender.hasPermission("matchvault.commands.history.others")) return emptyList()
        val currentArg = args.lastOrNull() ?: ""

        return Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.startsWith(currentArg, true) }
    }
}