package me.hosairis.matchvault.storage.database.cache

import de.marcely.bedwars.api.arena.Arena
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

internal object MatchCache {
    private val idByArena = ConcurrentHashMap<Arena, Long>()
    private val IdByPlayer = ConcurrentHashMap<UUID, Long>()

    fun getId(arena: Arena): Long? = idByArena[arena]
    fun getId(uuid: UUID): Long? = IdByPlayer[uuid]

    fun put(arena: Arena, id: Long) {
        idByArena[arena] = id
        arena.players.forEach { IdByPlayer[it.uniqueId] = id }
    }

    fun remove(arena: Arena) {
        val id = idByArena.remove(arena)
        IdByPlayer.entries.removeIf { it.value == id }
    }
}