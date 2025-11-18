package me.hosairis.matchvault.storage.database.data

import de.marcely.bedwars.api.game.shop.ShopOpenCause
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.ShopPurchases
import me.hosairis.matchvault.storage.database.runInTransaction
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Transaction
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
    var openCause: ShopOpenCause
) {
    var id: Long? = null
        private set

    companion object {
        suspend fun read(id: Long, transaction: Transaction? = null): ShopPurchaseData? = runInTransaction(transaction) {
            ShopPurchases
                .selectAll()
                .where { ShopPurchases.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        suspend fun readByMatchId(matchId: Long, transaction: Transaction? = null): List<ShopPurchaseData> =
            runInTransaction(transaction) {
                val matchRef = EntityID(matchId, Matches)
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.matchId eq matchRef }
                    .map { it.toData() }
            }

        suspend fun readByPlayerId(playerId: Long, transaction: Transaction? = null): List<ShopPurchaseData> =
            runInTransaction(transaction) {
                val playerRef = EntityID(playerId, Players)
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.playerId eq playerRef }
                    .map { it.toData() }
            }

        suspend fun readByTeamId(teamId: Long, transaction: Transaction? = null): List<ShopPurchaseData> =
            runInTransaction(transaction) {
                val teamRef = EntityID(teamId, MatchTeams)
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.teamId eq teamRef }
                    .map { it.toData() }
            }

        suspend fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit,
            transaction: Transaction? = null
        ): Boolean = runInTransaction(transaction) {
            ShopPurchases.update({ ShopPurchases.id eq id }) { statement ->
                val fetchRow = {
                    ShopPurchases
                        .selectAll()
                        .where { ShopPurchases.id eq id }
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }

        suspend fun updateByMatchId(
            matchId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit,
            transaction: Transaction? = null
        ): Boolean = runInTransaction(transaction) {
            val matchRef = EntityID(matchId, Matches)
            ShopPurchases.update({ ShopPurchases.matchId eq matchRef }) { statement ->
                val fetchRow = {
                    ShopPurchases
                        .selectAll()
                        .where { ShopPurchases.matchId eq matchRef }
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }

        suspend fun updateByPlayerId(
            playerId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit,
            transaction: Transaction? = null
        ): Boolean = runInTransaction(transaction) {
            val playerRef = EntityID(playerId, Players)
            ShopPurchases.update({ ShopPurchases.playerId eq playerRef }) { statement ->
                val fetchRow = {
                    ShopPurchases
                        .selectAll()
                        .where { ShopPurchases.playerId eq playerRef }
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }

        suspend fun updateByTeamId(
            teamId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit,
            transaction: Transaction? = null
        ): Boolean = runInTransaction(transaction) {
            val teamRef = EntityID(teamId, MatchTeams)
            ShopPurchases.update({ ShopPurchases.teamId eq teamRef }) { statement ->
                val fetchRow = {
                    ShopPurchases
                        .selectAll()
                        .where { ShopPurchases.teamId eq teamRef }
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
                openCause = this[ShopPurchases.openCause]
            ).apply {
                id = this@toData[ShopPurchases.id].value
            }
    }

    suspend fun create(transaction: Transaction? = null): Boolean = runInTransaction(transaction) {
        val newId = ShopPurchases.insertAndGetId { statement ->
            statement[ShopPurchases.matchId] = EntityID(this@ShopPurchaseData.matchId, Matches)
            statement[ShopPurchases.playerId] = EntityID(this@ShopPurchaseData.playerId, Players)
            statement[ShopPurchases.teamId] = EntityID(this@ShopPurchaseData.teamId, MatchTeams)
            statement[ShopPurchases.item] = this@ShopPurchaseData.item
            statement[ShopPurchases.amount] = this@ShopPurchaseData.amount
            statement[ShopPurchases.openCause] = this@ShopPurchaseData.openCause
        }
        this@ShopPurchaseData.id = newId.value
        true
    }

    suspend fun update(
        builder: (UpdateBuilder<Int>.(ShopPurchaseData) -> Unit)? = null,
        transaction: Transaction? = null
    ): Boolean = runInTransaction(transaction) {
        val recordId = id ?: return@runInTransaction false
        ShopPurchases.update({ ShopPurchases.id eq recordId }) { statement ->
            if (builder == null) {
                statement[ShopPurchases.item] = this@ShopPurchaseData.item
                statement[ShopPurchases.amount] = this@ShopPurchaseData.amount
                statement[ShopPurchases.openCause] = this@ShopPurchaseData.openCause
            } else {
                builder.invoke(statement, this@ShopPurchaseData)
            }
        } > 0
    }

    suspend fun delete(transaction: Transaction? = null): Boolean = runInTransaction(transaction) {
        val recordId = id ?: return@runInTransaction false
        ShopPurchases.deleteWhere { ShopPurchases.id eq recordId } > 0
    }
}
