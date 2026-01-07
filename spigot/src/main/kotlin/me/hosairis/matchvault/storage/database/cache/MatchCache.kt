package me.hosairis.matchvault.storage.database.cache

import de.marcely.bedwars.api.arena.Arena
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

internal object MatchCache {
    private val idByArena = ConcurrentHashMap<Arena, Long>()

    fun getId(arena: Arena): Long? = idByArena[arena]

    fun put(arena: Arena, id: Long) {
        idByArena[arena] = id
    }

    fun remove(arena: Arena) {
        idByArena.remove(arena)
    }
}