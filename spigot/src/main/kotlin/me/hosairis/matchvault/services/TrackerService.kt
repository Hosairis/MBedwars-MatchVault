package me.hosairis.matchvault.services

import de.marcely.bedwars.api.arena.Arena
import me.hosairis.matchvault.storage.database.data.MatchData
import me.hosairis.matchvault.storage.database.data.PlayerData
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TrackerService {
    val playerIdMap: MutableMap<UUID, PlayerData> = ConcurrentHashMap()
    val matchIdMap: MutableMap<Arena, MatchData> = ConcurrentHashMap()
}

