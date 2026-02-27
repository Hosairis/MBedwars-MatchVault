package me.hosairis.matchvault.update.listener

import me.hosairis.matchvault.storage.config.Messages
import me.hosairis.matchvault.update.UpdateChecker
import me.hosairis.matchvault.util.MessageHelper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener: Listener {
    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        if (!event.player.hasPermission("matchvault.events.notifyupdate")) return
        if (UpdateChecker.isOutdated) MessageHelper.sendMessage(event.player, Messages.values.updateFound)
    }
}