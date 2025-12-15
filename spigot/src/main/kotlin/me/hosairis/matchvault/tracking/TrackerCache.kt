package me.hosairis.matchvault.tracking

import de.marcely.bedwars.api.arena.Arena
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TrackerCache {

    // Minecraft UUID -> DB players.id
    val playerIds: MutableMap<UUID, Long> = ConcurrentHashMap()

    // Arena -> DB matches.id (current ongoing match for that arena on this server)
    val matchIds: MutableMap<Arena, Long> = ConcurrentHashMap()

    fun clearAll() {
        playerIds.clear()
        matchIds.clear()
    }
}