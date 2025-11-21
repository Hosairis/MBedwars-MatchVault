package me.hosairis.matchvault.services

import de.marcely.bedwars.api.arena.Arena
import java.util.concurrent.ConcurrentHashMap

object TrackerService {
    val matchIds: MutableMap<Arena, Long> = ConcurrentHashMap()
}

