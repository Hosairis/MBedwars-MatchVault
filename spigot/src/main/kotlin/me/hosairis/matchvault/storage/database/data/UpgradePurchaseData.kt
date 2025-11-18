package me.hosairis.matchvault.storage.database.data

import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import me.hosairis.matchvault.storage.database.Players
import me.hosairis.matchvault.storage.database.UpgradePurchases
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
        suspend fun read(id: Long, transaction: Transaction? = null): UpgradePurchaseData? = runInTransaction(transaction) {
            UpgradePurchases
                .selectAll()
                .where { UpgradePurchases.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        suspend fun readByMatchId(matchId: Long, transaction: Transaction? = null): List<UpgradePurchaseData> =
            runInTransaction(transaction) {
                val matchRef = EntityID(matchId, Matches)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.matchId eq matchRef }
                    .map { it.toData() }
            }

        suspend fun readByPlayerId(buyerId: Long, transaction: Transaction? = null): List<UpgradePurchaseData> =
            runInTransaction(transaction) {
                val buyerRef = EntityID(buyerId, Players)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.buyerId eq buyerRef }
                    .map { it.toData() }
            }

        suspend fun readByTeamId(teamId: Long, transaction: Transaction? = null): List<UpgradePurchaseData> =
            runInTransaction(transaction) {
                val teamRef = EntityID(teamId, MatchTeams)
                UpgradePurchases
                    .selectAll()
                    .where { UpgradePurchases.teamId eq teamRef }
                    .map { it.toData() }
            }

        suspend fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit,
            transaction: Transaction? = null
        ): Boolean = runInTransaction(transaction) {
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

        suspend fun updateByMatchId(
            matchId: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit,
            transaction: Transaction? = null
        ): Boolean = runInTransaction(transaction) {
            val matchRef = EntityID(matchId, Matches)
            UpgradePurchases.update({ UpgradePurchases.matchId eq matchRef }) { statement ->
                val fetchRow = {
                    UpgradePurchases
                        .selectAll()
                        .where { UpgradePurchases.matchId eq matchRef }
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
            val buyerRef = EntityID(playerId, Players)
            UpgradePurchases.update({ UpgradePurchases.buyerId eq buyerRef }) { statement ->
                val fetchRow = {
                    UpgradePurchases
                        .selectAll()
                        .where { UpgradePurchases.buyerId eq buyerRef }
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
            UpgradePurchases.update({ UpgradePurchases.teamId eq teamRef }) { statement ->
                val fetchRow = {
                    UpgradePurchases
                        .selectAll()
                        .where { UpgradePurchases.teamId eq teamRef }
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
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

    suspend fun create(transaction: Transaction? = null): Boolean = runInTransaction(transaction) {
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

    suspend fun update(
        builder: (UpdateBuilder<Int>.(UpgradePurchaseData) -> Unit)? = null,
        transaction: Transaction? = null
    ): Boolean = runInTransaction(transaction) {
        val recordId = id ?: return@runInTransaction false
        UpgradePurchases.update({ UpgradePurchases.id eq recordId }) { statement ->
            if (builder == null) {
                statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
                statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
            } else {
                builder.invoke(statement, this@UpgradePurchaseData)
            }
        } > 0
    }

    suspend fun delete(transaction: Transaction? = null): Boolean = runInTransaction(transaction) {
        val recordId = id ?: return@runInTransaction false
        UpgradePurchases.deleteWhere { UpgradePurchases.id eq recordId } > 0
    }
}
