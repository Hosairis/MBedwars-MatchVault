package me.hosairis.matchvault.gui

import de.marcely.bedwars.tools.Helper
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.hosairis.matchvault.storage.database.model.MatchPlayerData
import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.storage.database.service.PlayerService
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.util.MessageHelper
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object PlayerListGui {

    fun open(player: Player, teamId: Long, target: String? = null) {
        val gui = createGui()
        val name = target ?: player.name
        gui.setItem(0, CommonGuiItems.backItem { MatchListGui.open(player, target) })

        gui.open(player)

        CoroutineHelper.runAsync {
            val matchPlayerList = MatchService.readPlayersOfTeam(teamId)

            CoroutineHelper.runSync {
                val playerId = PlayerService.readIdByName(name) ?: -1
                for (matchPlayerData in matchPlayerList) {
                    gui.addItem(createPlayerItem(matchPlayerData, matchPlayerData.playerId == playerId))
                }
                gui.update()
            }
        }
    }

    private fun createGui(): Gui {
        val gui = Gui
            .gui()
            .title(Component.text("Player List"))
            .rows(6)
            .disableAllInteractions()
            .create()

        gui.filler.fillBorder(CommonGuiItems.fillerItem)

        return gui
    }

    fun createPlayerItem(matchPlayerData: MatchPlayerData, self: Boolean): GuiItem {
        val uuid = PlayerService.readUuidById(matchPlayerData.playerId)
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)

        return ItemBuilder
            .skull()
            .owner(offlinePlayer)
            .name(Component.text(MessageHelper.colorize("&b${offlinePlayer.name} ${if (self) "&7(YOU)" else ""}")))
            .lore(
                Component.text(MessageHelper.colorize("&r")),
                Component.text(MessageHelper.colorize("&7Kills: &f${matchPlayerData.kills}")),
                Component.text(MessageHelper.colorize("&7Final Kills: &f${matchPlayerData.finalKills}")),
                Component.text(MessageHelper.colorize("&7Deaths: &f${matchPlayerData.deaths}")),
                Component.text(MessageHelper.colorize("&7Beds Destroyed: &f${matchPlayerData.bedsDestroyed}")),
                Component.text(MessageHelper.colorize("&7Total Irons Collected: &f${matchPlayerData.resIron}")),
                Component.text(MessageHelper.colorize("&7Total Golds Collected: &f${matchPlayerData.resGold}")),
                Component.text(MessageHelper.colorize("&7Total Diamonds Collected: &f${matchPlayerData.resDiamond}")),
                Component.text(MessageHelper.colorize("&7Total Emerald Collected: &f${matchPlayerData.resEmerald}")),
                Component.text(MessageHelper.colorize("&7Spawner Irons Collected: &f${matchPlayerData.resIronSpawner}")),
                Component.text(MessageHelper.colorize("&7Spawner Golds Collected: &f${matchPlayerData.resGoldSpawner}")),
                Component.text(MessageHelper.colorize("&7Spawner Diamonds Collected: &f${matchPlayerData.resDiamondSpawner}")),
                Component.text(MessageHelper.colorize("&7Spawner Emerald Collected: &f${matchPlayerData.resEmeraldSpawner}")),
                Component.text(MessageHelper.colorize("&7Status: ${if (matchPlayerData.won) "&aWinner" else "&4Loser"}"))
            )
            .asGuiItem()
    }
}