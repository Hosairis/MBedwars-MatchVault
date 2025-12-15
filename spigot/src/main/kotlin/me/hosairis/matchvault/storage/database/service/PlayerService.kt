package me.hosairis.matchvault.storage.database.service

import me.hosairis.matchvault.storage.database.model.PlayerData
import me.hosairis.matchvault.storage.database.repo.PlayerRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class PlayerService(
    private val playerRepo: PlayerRepository
) {

    fun upsertSeen(uuid: UUID, name: String, seenAt: Long = System.currentTimeMillis()): PlayerData = transaction {
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
            playerRepo.read(newId) ?: throw NullPointerException("Failed to read created player id=$newId")
        } else {
            val updated = existing.copy(name = name, lastSeen = seenAt)
            playerRepo.update(updated)
            updated
        }
    }

    fun read(id: Long): PlayerData? = transaction {
        playerRepo.read(id)
    }

    fun readByUuid(uuid: UUID): PlayerData? = transaction {
        playerRepo.readByUuid(uuid)
    }

    fun delete(id: Long): Boolean = transaction {
        playerRepo.delete(id)
    }
}
