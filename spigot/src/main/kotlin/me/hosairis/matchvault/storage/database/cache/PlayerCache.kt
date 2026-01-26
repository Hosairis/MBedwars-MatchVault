package me.hosairis.matchvault.storage.database.cache

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object PlayerCache {
    private val idByUuid = ConcurrentHashMap<UUID, Long>()
    private val idByName = ConcurrentHashMap<String, Long>()

    fun getId(uuid: UUID): Long? = idByUuid[uuid]

    fun getId(name: String): Long? = idByName[name]

    fun put(uuid: UUID, name: String, id: Long) {
        idByUuid[uuid] = id
        idByName[name] = id
    }

    fun remove(uuid: UUID, name: String) {
        idByUuid.remove(uuid)
        idByName.remove(name)
    }
}