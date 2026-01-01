package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.PlayerData
import me.hosairis.matchvault.storage.database.tables.Players
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class PlayerRepository {

    fun create(data: PlayerData): Long {
        return Players.insertAndGetId {
            it[name] = data.name
            it[uuid] = data.uuid.toString()
            it[firstSeen] = data.firstSeen
            it[lastSeen] = data.lastSeen
        }.value
    }

    fun update(data: PlayerData): Boolean {
        val exists = Players
            .select(Players.id)
            .where { Players.id eq data.id }
            .forUpdate()
            .limit(1)
            .any()

        if (!exists) return false

        return Players.update({ Players.id eq data.id }) { st ->
            st[name] = data.name
            st[uuid] = data.uuid.toString()
            st[firstSeen] = data.firstSeen
            st[lastSeen] = data.lastSeen
        } > 0
    }

    fun delete(id: Long): Boolean {
        return Players.deleteWhere { Players.id eq id } > 0
    }

    fun read(id: Long): PlayerData? {
        return Players
            .selectAll()
            .where { Players.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByUuid(uuid: UUID): PlayerData? {
        return Players
            .selectAll()
            .where { Players.uuid eq uuid.toString() }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readIdByUuid(uuid: UUID): Long? {
        return Players
            .select(Players.id)
            .where { Players.uuid eq uuid.toString() }
            .limit(1)
            .firstOrNull()
            ?.get(Players.id)
            ?.value
    }

    private fun ResultRow.toData(): PlayerData {
        return PlayerData(
            id = this[Players.id].value,
            name = this[Players.name],
            uuid = UUID.fromString(this[Players.uuid]),
            firstSeen = this[Players.firstSeen],
            lastSeen = this[Players.lastSeen]
        )
    }
}
