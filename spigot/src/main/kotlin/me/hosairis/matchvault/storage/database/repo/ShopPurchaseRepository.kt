package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.ShopPurchaseData
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import me.hosairis.matchvault.storage.database.tables.Players
import me.hosairis.matchvault.storage.database.tables.ShopPurchases
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ShopPurchaseRepository {

    fun create(data: ShopPurchaseData): Long {
        return ShopPurchases.insertAndGetId {
            it[matchId] = EntityID(data.matchId, Matches)
            it[playerId] = EntityID(data.playerId, Players)
            it[teamId] = EntityID(data.teamId, MatchTeams)
            it[item] = data.item
            it[amount] = data.amount
            it[itemType] = data.itemType
            it[openCause] = data.openCause
        }.value
    }

    fun update(data: ShopPurchaseData): Boolean {
        val exists = ShopPurchases
            .select(ShopPurchases.id)
            .where { ShopPurchases.id eq data.id }
            .forUpdate()
            .limit(1)
            .any()

        if (!exists) return false

        return ShopPurchases.update({ ShopPurchases.id eq data.id }) {
            it[teamId] = EntityID(data.teamId, MatchTeams)
            it[item] = data.item
            it[amount] = data.amount
            it[itemType] = data.itemType
            it[openCause] = data.openCause
        } > 0
    }

    fun delete(id: Long): Boolean {
        return ShopPurchases.deleteWhere { ShopPurchases.id eq id } > 0
    }

    fun read(id: Long): ShopPurchaseData? {
        return ShopPurchases
            .selectAll()
            .where { ShopPurchases.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchId(matchId: Long): List<ShopPurchaseData> {
        return ShopPurchases
            .selectAll()
            .where { ShopPurchases.matchId eq matchId }
            .orderBy(ShopPurchases.id to SortOrder.ASC)
            .map { it.toData() }
    }

    fun readByPlayerId(playerId: Long): List<ShopPurchaseData> {
        return ShopPurchases
            .selectAll()
            .where { ShopPurchases.playerId eq playerId }
            .orderBy(ShopPurchases.id to SortOrder.DESC)
            .map { it.toData() }
    }

    private fun ResultRow.toData(): ShopPurchaseData {
        return ShopPurchaseData(
            id = this[ShopPurchases.id].value,
            matchId = this[ShopPurchases.matchId].value,
            playerId = this[ShopPurchases.playerId].value,
            teamId = this[ShopPurchases.teamId].value,
            item = this[ShopPurchases.item],
            amount = this[ShopPurchases.amount],
            itemType = this[ShopPurchases.itemType],
            openCause = this[ShopPurchases.openCause]
        )
    }
}
