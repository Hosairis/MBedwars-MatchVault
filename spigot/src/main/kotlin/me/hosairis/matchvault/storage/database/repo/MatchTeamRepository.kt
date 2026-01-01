package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.model.MatchTeamData
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchTeamRepository {

    fun create(data: MatchTeamData): Long {
        return MatchTeams.insertAndGetId {
            it[matchId] = EntityID(data.matchId, Matches)
            it[team] = data.team
            it[bedDestroyedAt] = data.bedDestroyedAt
            it[eliminatedAt] = data.eliminatedAt
            it[finalPlacement] = data.finalPlacement
        }.value
    }

    fun update(data: MatchTeamData): Boolean {
        val exists = MatchTeams
            .select(MatchTeams.id)
            .where { MatchTeams.id eq data.id }
            .forUpdate()
            .limit(1)
            .any()

        if (!exists) return false

        return MatchTeams.update({ MatchTeams.id eq data.id }) { st ->
            st[team] = data.team
            st[bedDestroyedAt] = data.bedDestroyedAt
            st[eliminatedAt] = data.eliminatedAt
            st[finalPlacement] = data.finalPlacement
        } > 0
    }

    fun delete(id: Long): Boolean {
        return MatchTeams.deleteWhere { MatchTeams.id eq id } > 0
    }

    fun read(id: Long): MatchTeamData? {
        return MatchTeams
            .selectAll()
            .where { MatchTeams.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun readByMatchId(matchId: Long): List<MatchTeamData> {
        return MatchTeams
            .selectAll()
            .where { MatchTeams.matchId eq matchId }
            .orderBy(MatchTeams.id to SortOrder.ASC)
            .map { it.toData() }
    }

    fun readByMatchIdAndTeam(matchId: Long, team: String): MatchTeamData? {
        return MatchTeams
            .selectAll()
            .where { (MatchTeams.matchId eq matchId) and (MatchTeams.team eq team) }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    private fun ResultRow.toData(): MatchTeamData {
        return MatchTeamData(
            id = this[MatchTeams.id].value,
            matchId = this[MatchTeams.matchId].value,
            team = this[MatchTeams.team],
            bedDestroyedAt = this[MatchTeams.bedDestroyedAt],
            eliminatedAt = this[MatchTeams.eliminatedAt],
            finalPlacement = this[MatchTeams.finalPlacement]
        )
    }
}
