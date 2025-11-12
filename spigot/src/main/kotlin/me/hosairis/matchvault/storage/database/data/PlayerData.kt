package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.Players
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
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

    var stats: PlayerStatsData? = null
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
                            name = row[Players.name],
                            uuid = UUID.fromString(row[Players.uuid]),
                            firstSeen = row[Players.firstSeen],
                            lastSeen = row[Players.lastSeen]
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
                val newId = Players.insertAndGetId { statement ->
                    statement[Players.name] = this@PlayerData.name
                    statement[Players.uuid] = this@PlayerData.uuid.toString()
                    statement[Players.firstSeen] = this@PlayerData.firstSeen
                    statement[Players.lastSeen] = this@PlayerData.lastSeen
                }
                this@PlayerData.id = newId.value
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(
        updateFirstSeen: Boolean = false,
        builder: (UpdateBuilder<Int>.(PlayerData) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                Players.update({ Players.uuid eq uuid.toString() }) { statement ->
                    if (builder == null) {
                        statement[Players.name] = this@PlayerData.name
                        if (updateFirstSeen) statement[Players.firstSeen] = this@PlayerData.firstSeen
                        statement[Players.lastSeen] = this@PlayerData.lastSeen
                    } else {
                        builder.invoke(statement, this@PlayerData)
                    }
                } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                Players.deleteWhere { Players.uuid eq uuid.toString() } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun loadStats() {
        val playerId = id ?: return
        stats = PlayerStatsData.readByPlayerId(playerId)
    }
}
