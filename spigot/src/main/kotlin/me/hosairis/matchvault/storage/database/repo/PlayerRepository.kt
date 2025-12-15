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

    fun create(data: PlayerData): Long =
        Players.insertAndGetId {
            it[name] = data.name
            it[uuid] = data.uuid.toString()
            it[firstSeen] = data.firstSeen
            it[lastSeen] = data.lastSeen
        }.value

    fun read(id: Long): PlayerData? =
        Players
            .selectAll()
            .where { Players.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readByUuid(uuid: UUID): PlayerData? =
        Players
            .selectAll()
            .where { Players.uuid eq uuid.toString() }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readIdByUuid(uuid: UUID): Long? =
        Players
            .select(Players.id)
            .where { Players.uuid eq uuid.toString() }
            .limit(1)
            .firstOrNull()
            ?.get(Players.id)
            ?.value

    fun update(data: PlayerData): Boolean =
        Players.update({ Players.id eq data.id }) { st ->
            st[name] = data.name
            st[uuid] = data.uuid.toString()
            st[firstSeen] = data.firstSeen
            st[lastSeen] = data.lastSeen
        } > 0

    fun delete(id: Long): Boolean =
        Players.deleteWhere { Players.id eq id } > 0

    private fun ResultRow.toData(): PlayerData = PlayerData(
        id = this[Players.id].value,
        name = this[Players.name],
        uuid = UUID.fromString(this[Players.uuid]),
        firstSeen = this[Players.firstSeen],
        lastSeen = this[Players.lastSeen]
    )
}
