package me.hosairis.matchvault.storage.database.data

import me.hosairis.matchvault.storage.database.Players
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
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
        fun read(uuid: UUID): PlayerData? {
            return Players
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

        fun update(
            uuid: UUID,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return Players.update({ Players.uuid eq uuid.toString() }) { statement ->
                val fetchRow = {
                    Players
                        .selectAll()
                        .where { Players.uuid eq uuid.toString() }
                        .forUpdate()
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }
    }

    fun create(): Boolean {
        val newId = Players.insertAndGetId { statement ->
            statement[Players.name] = this@PlayerData.name
            statement[Players.uuid] = this@PlayerData.uuid.toString()
            statement[Players.firstSeen] = this@PlayerData.firstSeen
            statement[Players.lastSeen] = this@PlayerData.lastSeen
        }
        this@PlayerData.id = newId.value
        return true
    }

    fun update(
        updateFirstSeen: Boolean = false,
        builder: (UpdateBuilder<Int>.(PlayerData) -> Unit)? = null
    ): Boolean {
        return Players.update({ Players.uuid eq uuid.toString() }) { statement ->
            if (builder == null) {
                statement[Players.name] = this@PlayerData.name
                if (updateFirstSeen) statement[Players.firstSeen] = this@PlayerData.firstSeen
                statement[Players.lastSeen] = this@PlayerData.lastSeen
            } else {
                builder.invoke(statement, this@PlayerData)
            }
        } > 0
    }

    fun delete(): Boolean {
        return Players.deleteWhere { Players.uuid eq uuid.toString() } > 0
    }
}
