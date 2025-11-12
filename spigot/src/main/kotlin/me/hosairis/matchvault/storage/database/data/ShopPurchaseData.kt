package me.hosairis.matchvault.storage.database.data

import de.marcely.bedwars.api.game.shop.ShopOpenCause
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
        suspend fun read(id: Long): ShopPurchaseData? = withContext(Dispatchers.IO) {
            transaction {
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByMatchId(matchId: Long): List<ShopPurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val matchRef = EntityID(matchId, Matches)
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.matchId eq matchRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByPlayerId(playerId: Long): List<ShopPurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val playerRef = EntityID(playerId, Players)
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.playerId eq playerRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByTeamId(teamId: Long): List<ShopPurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val teamRef = EntityID(teamId, MatchTeams)
                ShopPurchases
                    .selectAll()
                    .where { ShopPurchases.teamId eq teamRef }
                    .map { it.toData() }
            }
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

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
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
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(builder: (UpdateBuilder<Int>.(ShopPurchaseData) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            try {
                ShopPurchases.update({ ShopPurchases.id eq recordId }) { statement ->
                    if (builder == null) {
                        statement[ShopPurchases.item] = this@ShopPurchaseData.item
                        statement[ShopPurchases.amount] = this@ShopPurchaseData.amount
                        statement[ShopPurchases.openCause] = this@ShopPurchaseData.openCause
                    } else {
                        builder.invoke(statement, this@ShopPurchaseData)
                    }
                } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            try {
                ShopPurchases.deleteWhere { ShopPurchases.id eq recordId } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }
}
