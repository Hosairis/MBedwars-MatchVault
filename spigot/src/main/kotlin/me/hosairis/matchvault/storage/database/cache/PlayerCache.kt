package me.hosairis.matchvault.storage.database.cache

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object PlayerCache {
    private val idByUuid = ConcurrentHashMap<UUID, Long>()

    fun getId(uuid: UUID): Long? = idByUuid[uuid]

    fun put(uuid: UUID, id: Long) {
        idByUuid[uuid] = id
    }

    fun remove(uuid: UUID) {
        idByUuid.remove(uuid)
    }
}