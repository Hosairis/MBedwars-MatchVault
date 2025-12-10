package me.hosairis.matchvault.services.trackingservice

import de.marcely.bedwars.api.arena.Arena
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TrackerService {
    val playerIds: MutableMap<UUID, Long> = ConcurrentHashMap()
    val matchIds: MutableMap<Arena, Long> = ConcurrentHashMap()
}

