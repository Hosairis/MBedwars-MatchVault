package me.hosairis.matchvault.storage.database.data

import de.marcely.bedwars.api.game.shop.ShopOpenCause
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.ShopPurchases
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

data class ShopPurchaseData(
    val matchId: Long,
    val playerId: Long,
    val teamId: Long,
    var item: String,
    var amount: Int = 1,
    var itemType: ShopProductType,
    var openCause: ShopOpenCause
) {
    var id: Long? = null
        private set

    companion object {
        fun read(id: Long): ShopPurchaseData? {
            return ShopPurchases
                .selectAll()
                .where { ShopPurchases.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        fun readByMatchId(matchId: Long): List<ShopPurchaseData> {
            val matchRef = EntityID(matchId, Matches)
            return ShopPurchases
                .selectAll()
                .where { ShopPurchases.matchId eq matchRef }
                .map { it.toData() }
        }

        fun readByPlayerId(playerId: Long): List<ShopPurchaseData> {
            val playerRef = EntityID(playerId, Players)
            return ShopPurchases
                .selectAll()
                .where { ShopPurchases.playerId eq playerRef }
                .map { it.toData() }
        }

        fun readByTeamId(teamId: Long): List<ShopPurchaseData> {
            val teamRef = EntityID(teamId, MatchTeams)
            return ShopPurchases
                .selectAll()
                .where { ShopPurchases.teamId eq teamRef }
                .map { it.toData() }
        }

        fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return ShopPurchases.update({ ShopPurchases.id eq id }) { statement ->
                val fetchRow = {
                    ShopPurchases
                        .selectAll()
                        .where { ShopPurchases.id eq id }
                        .forUpdate()
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }

        private fun ResultRow.toData(): ShopPurchaseData =
            ShopPurchaseData(
                matchId = this[ShopPurchases.matchId].value,
                playerId = this[ShopPurchases.playerId].value,
                teamId = this[ShopPurchases.teamId].value,
                item = this[ShopPurchases.item],
                amount = this[ShopPurchases.amount],
                itemType = this[ShopPurchases.itemType],
                openCause = this[ShopPurchases.openCause]
            ).apply {
                id = this@toData[ShopPurchases.id].value
            }
    }

    fun create(): Boolean {
        val newId = ShopPurchases.insertAndGetId { statement ->
            statement[ShopPurchases.matchId] = EntityID(this@ShopPurchaseData.matchId, Matches)
            statement[ShopPurchases.playerId] = EntityID(this@ShopPurchaseData.playerId, Players)
            statement[ShopPurchases.teamId] = EntityID(this@ShopPurchaseData.teamId, MatchTeams)
            statement[ShopPurchases.item] = this@ShopPurchaseData.item
            statement[ShopPurchases.amount] = this@ShopPurchaseData.amount
            statement[ShopPurchases.itemType] = this@ShopPurchaseData.itemType
            statement[ShopPurchases.openCause] = this@ShopPurchaseData.openCause
        }
        this@ShopPurchaseData.id = newId.value
        return true
    }

    fun update(builder: (UpdateBuilder<Int>.(ShopPurchaseData) -> Unit)? = null): Boolean {
        val recordId = id ?: return false
        return ShopPurchases.update({ ShopPurchases.id eq recordId }) { statement ->
            if (builder == null) {
                statement[ShopPurchases.item] = this@ShopPurchaseData.item
                statement[ShopPurchases.amount] = this@ShopPurchaseData.amount
                statement[ShopPurchases.itemType] = this@ShopPurchaseData.itemType
                statement[ShopPurchases.openCause] = this@ShopPurchaseData.openCause
            } else {
                builder.invoke(statement, this@ShopPurchaseData)
            }
        } > 0
    }

    fun delete(): Boolean {
        val recordId = id ?: return false
        return ShopPurchases.deleteWhere { ShopPurchases.id eq recordId } > 0
    }
}
