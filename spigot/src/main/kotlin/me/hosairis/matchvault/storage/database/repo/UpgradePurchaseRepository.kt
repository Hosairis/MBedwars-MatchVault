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
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class UpgradePurchaseRepository {

    fun create(data: UpgradePurchaseData): Long =
        UpgradePurchases.insertAndGetId {
            it[matchId] = EntityID(data.matchId, Matches)
            it[playerId] = EntityID(data.playerId, Players)
            it[teamId] = EntityID(data.teamId, MatchTeams)
            it[upgrade] = data.upgrade
            it[level] = data.level
        }.value

    fun read(id: Long): UpgradePurchaseData? =
        UpgradePurchases
            .selectAll()
            .where { UpgradePurchases.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()

    fun readByMatchId(matchId: Long): List<UpgradePurchaseData> =
        UpgradePurchases
            .selectAll()
            .where { UpgradePurchases.matchId eq matchId }
            .orderBy(UpgradePurchases.id to SortOrder.ASC)
            .map { it.toData() }

    fun readByPlayerId(buyerId: Long): List<UpgradePurchaseData> =
        UpgradePurchases
            .selectAll()
            .where { UpgradePurchases.playerId eq buyerId }
            .orderBy(UpgradePurchases.id to SortOrder.DESC)
            .map { it.toData() }

    fun update(data: UpgradePurchaseData): Boolean =
        UpgradePurchases.update({ UpgradePurchases.id eq data.id }) {
            it[teamId] = EntityID(data.teamId, MatchTeams)
            it[upgrade] = data.upgrade
            it[level] = data.level
        } > 0

    fun delete(id: Long): Boolean =
        UpgradePurchases.deleteWhere { UpgradePurchases.id eq id } > 0

    fun deleteByMatchId(matchId: Long): Int =
        UpgradePurchases.deleteWhere { UpgradePurchases.matchId eq matchId }

    private fun ResultRow.toData(): UpgradePurchaseData = UpgradePurchaseData(
        id = this[UpgradePurchases.id].value,
        matchId = this[UpgradePurchases.matchId].value,
        playerId = this[UpgradePurchases.playerId].value,
        teamId = this[UpgradePurchases.teamId].value,
        upgrade = this[UpgradePurchases.upgrade],
        level = this[UpgradePurchases.level]
    )
}
