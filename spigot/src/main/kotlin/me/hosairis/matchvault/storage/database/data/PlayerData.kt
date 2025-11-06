package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.Players
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

data class PlayerData(
    var name: String,
    val uuid: UUID,
    var firstSeen: Long,
    var lastSeen: Long
) {
    var id: Long? = null
        private set

    companion object {
        suspend fun read(uuid: UUID): PlayerData? = withContext(Dispatchers.IO) {
            transaction {
                Players
                    .selectAll()
                    .where { Players.uuid eq uuid.toString() }
                    .limit(1)
                    .firstOrNull()
                    ?.let { row ->
                        PlayerData(
                            row[Players.name],
                            UUID.fromString(row[Players.uuid]),
                            row[Players.firstSeen],
                            row[Players.lastSeen]
                        ).apply {
                            id = row[Players.id].value
                        }
                    }
            }
        }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                val newId = Players.insertAndGetId {
                    it[Players.name] = this@PlayerData.name
                    it[Players.uuid] = this@PlayerData.uuid.toString()
                    it[Players.firstSeen] = this@PlayerData.firstSeen
                    it[Players.lastSeen] = this@PlayerData.lastSeen
                }
                this@PlayerData.id = newId.value
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(updateFirstSeen: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                val rowsUpdated = Players.update({ Players.uuid eq uuid.toString() }) {
                    it[name] = this@PlayerData.name
                    if (updateFirstSeen) it[firstSeen] = this@PlayerData.firstSeen
                    it[lastSeen] = this@PlayerData.lastSeen
                }
                rowsUpdated > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                val rowsDeleted = Players.deleteWhere { Players.uuid eq uuid }
                rowsDeleted > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }
}
