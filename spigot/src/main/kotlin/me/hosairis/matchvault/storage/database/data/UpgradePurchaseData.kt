package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.UpgradePurchases
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

data class UpgradePurchaseData(
    val matchId: Long,
    val buyerId: Long,
    val teamId: Long,
    var upgrade: String,
    var level: Int = 1
) {
    var id: Long? = null
        private set

    companion object {
        suspend fun read(id: Long): UpgradePurchaseData? = withContext(Dispatchers.IO) {
            transaction {
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByMatchId(matchId: Long): List<UpgradePurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val matchRef = EntityID(matchId, Matches)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.matchId eq matchRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByPlayerId(buyerId: Long): List<UpgradePurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val buyerRef = EntityID(buyerId, Players)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.buyerId eq buyerRef }
                    .map { it.toData() }
            }
        }

        suspend fun readByTeamId(teamId: Long): List<UpgradePurchaseData> = withContext(Dispatchers.IO) {
            transaction {
                val teamRef = EntityID(teamId, MatchTeams)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.teamId eq teamRef }
                    .map { it.toData() }
            }
        }

        suspend fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            transaction {
                UpgradePurchases.update({ UpgradePurchases.id eq id }) { statement ->
                    val fetchRow = {
                        UpgradePurchases
                            .selectAll()
                            .where { UpgradePurchases.id eq id }
                            .limit(1)
                            .firstOrNull()
                    }
                    builder(statement, fetchRow)
                } > 0
            }
        }

        private fun ResultRow.toData(): UpgradePurchaseData =
            UpgradePurchaseData(
                matchId = this[UpgradePurchases.matchId].value,
                buyerId = this[UpgradePurchases.buyerId].value,
                teamId = this[UpgradePurchases.teamId].value,
                upgrade = this[UpgradePurchases.upgrade],
                level = this[UpgradePurchases.level]
            ).apply {
                id = this@toData[UpgradePurchases.id].value
            }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            val newId = UpgradePurchases.insertAndGetId { statement ->
                statement[UpgradePurchases.matchId] = EntityID(this@UpgradePurchaseData.matchId, Matches)
                statement[UpgradePurchases.buyerId] = EntityID(this@UpgradePurchaseData.buyerId, Players)
                statement[UpgradePurchases.teamId] = EntityID(this@UpgradePurchaseData.teamId, MatchTeams)
                statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
                statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
            }
            this@UpgradePurchaseData.id = newId.value
            true
        }
    }

    suspend fun update(builder: (UpdateBuilder<Int>.(UpgradePurchaseData) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            UpgradePurchases.update({ UpgradePurchases.id eq recordId }) { statement ->
                if (builder == null) {
                    statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
                    statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
                } else {
                    builder.invoke(statement, this@UpgradePurchaseData)
                }
            } > 0
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val recordId = id ?: return@withContext false
        transaction {
            UpgradePurchases.deleteWhere { UpgradePurchases.id eq recordId } > 0
        }
    }
}
