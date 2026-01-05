package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.UpgradePurchaseData
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import me.hosairis.matchvault.storage.database.tables.Players
import me.hosairis.matchvault.storage.database.tables.UpgradePurchases
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class UpgradePurchaseRepository {

    fun create(data: UpgradePurchaseData): Long {
        return UpgradePurchases.insertAndGetId {
            it[matchId] = EntityID(data.matchId, Matches)
            it[playerId] = EntityID(data.playerId, Players)
            it[teamId] = EntityID(data.teamId, MatchTeams)
            it[upgrade] = data.upgrade
            it[level] = data.level
        }.value
    }

    fun update(data: UpgradePurchaseData): Boolean {
        return UpgradePurchases.update({ UpgradePurchases.id eq data.id }) {
            it[teamId] = EntityID(data.teamId, MatchTeams)
            it[upgrade] = data.upgrade
            it[level] = data.level
        } > 0
    }

    fun delete(id: Long): Boolean {
        return UpgradePurchases.deleteWhere { UpgradePurchases.id eq id } > 0
    }

    fun read(id: Long, forUpdate: Boolean = false): UpgradePurchaseData? {
        return UpgradePurchases
            .selectAll()
            .withForUpdate(forUpdate)
            .where { UpgradePurchases.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchId(matchId: Long, forUpdate: Boolean = false): List<UpgradePurchaseData> {
        return UpgradePurchases
            .selectAll()
            .withForUpdate(forUpdate)
            .where { UpgradePurchases.matchId eq matchId }
            .orderBy(UpgradePurchases.id to SortOrder.ASC)
            .map { it.toData() }
    }

    fun readByPlayerId(buyerId: Long, forUpdate: Boolean = false): List<UpgradePurchaseData> {
        return UpgradePurchases
            .selectAll()
            .withForUpdate(forUpdate)
            .where { UpgradePurchases.playerId eq buyerId }
            .orderBy(UpgradePurchases.id to SortOrder.DESC)
            .map { it.toData() }
    }

    private fun ResultRow.toData(): UpgradePurchaseData {
        return  UpgradePurchaseData(
            id = this[UpgradePurchases.id].value,
            matchId = this[UpgradePurchases.matchId].value,
            playerId = this[UpgradePurchases.playerId].value,
            teamId = this[UpgradePurchases.teamId].value,
            upgrade = this[UpgradePurchases.upgrade],
            level = this[UpgradePurchases.level]
        )
    }

    private fun Query.withForUpdate(forUpdate: Boolean): Query {
        return if (forUpdate) this.forUpdate() else this
    }
}
