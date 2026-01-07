package me.hosairis.matchvault.storage.database.service

import me.hosairis.matchvault.storage.database.cache.PlayerCache
import me.hosairis.matchvault.storage.database.model.PlayerData
import me.hosairis.matchvault.storage.database.repo.PlayerRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

object PlayerService {
    private val playerRepo = PlayerRepository()

    fun upsertSeen(
        uuid: UUID,
        name: String,
        seenAt: Long = System.currentTimeMillis(),
        cache: Boolean
    ): PlayerData = transaction {
        val existing = playerRepo.readByUuid(uuid)

        if (existing == null) {
            val newId = playerRepo.create(
                PlayerData(
                    uuid = uuid,
                    name = name,
                    firstSeen = seenAt,
                    lastSeen = seenAt
                )
            )
            if (cache) PlayerCache.put(uuid, newId) else PlayerCache.remove(uuid)
            playerRepo.read(newId) ?: throw NullPointerException("Failed to read created player id=$newId")
        } else {
            val updated = existing.copy(name = name, lastSeen = seenAt)
            playerRepo.update(updated)
            if (cache) PlayerCache.put(uuid, updated.id!!) else PlayerCache.remove(uuid)
            updated
        }
    }

    fun read(id: Long): PlayerData? = transaction {
        playerRepo.read(id)
    }

    fun readByUuid(uuid: UUID): PlayerData? = transaction {
        playerRepo.readByUuid(uuid)
    }

    fun readIdByUuid(uuid: UUID): Long? {
        return PlayerCache.getId(uuid) ?: transaction {
            playerRepo.readIdByUuid(uuid)
        }
    }

    fun delete(id: Long): Boolean = transaction {
        playerRepo.delete(id)
    }
}
