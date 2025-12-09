package me.hosairis.matchvault.storage.database.data

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
        fun read(id: Long): UpgradePurchaseData? {
            return UpgradePurchases
                .selectAll()
                .where { UpgradePurchases.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        fun readByMatchId(matchId: Long): List<UpgradePurchaseData> {
            val matchRef = EntityID(matchId, Matches)
            return UpgradePurchases
                .selectAll()
                .where { UpgradePurchases.matchId eq matchRef }
                .map { it.toData() }
        }

        fun readByPlayerId(buyerId: Long): List<UpgradePurchaseData> {
            val buyerRef = EntityID(buyerId, Players)
            return UpgradePurchases
                .selectAll()
                .where { UpgradePurchases.buyerId eq buyerRef }
                .map { it.toData() }
        }

        fun readByTeamId(teamId: Long): List<UpgradePurchaseData> {
            val teamRef = EntityID(teamId, MatchTeams)
            return UpgradePurchases
                .selectAll()
                .where { UpgradePurchases.teamId eq teamRef }
                .map { it.toData() }
        }

        fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return UpgradePurchases.update({ UpgradePurchases.id eq id }) { statement ->
                val fetchRow = {
                    UpgradePurchases
                        .selectAll()
                        .where { UpgradePurchases.id eq id }
                        .forUpdate()
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

    fun create(): Boolean {
        val newId = UpgradePurchases.insertAndGetId { statement ->
            statement[UpgradePurchases.matchId] = EntityID(this@UpgradePurchaseData.matchId, Matches)
            statement[UpgradePurchases.buyerId] = EntityID(this@UpgradePurchaseData.buyerId, Players)
            statement[UpgradePurchases.teamId] = EntityID(this@UpgradePurchaseData.teamId, MatchTeams)
            statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
            statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
        }
        this@UpgradePurchaseData.id = newId.value
        return true
    }

    fun update(builder: (UpdateBuilder<Int>.(UpgradePurchaseData) -> Unit)? = null): Boolean {
        val recordId = id ?: return false
        return UpgradePurchases.update({ UpgradePurchases.id eq recordId }) { statement ->
            if (builder == null) {
                statement[UpgradePurchases.upgrade] = this@UpgradePurchaseData.upgrade
                statement[UpgradePurchases.level] = this@UpgradePurchaseData.level
            } else {
                builder.invoke(statement, this@UpgradePurchaseData)
            }
        } > 0
    }

    fun delete(): Boolean {
        val recordId = id ?: return false
        return UpgradePurchases.deleteWhere { UpgradePurchases.id eq recordId } > 0
    }
}
