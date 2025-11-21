package me.hosairis.matchvault.storage.database.data

import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

data class MatchTeamData(
    val matchId: Long,
    var team: String,
    var bedDestroyedAt: Long? = null,
    var eliminatedAt: Long? = null,
    var finalPlacement: Int? = null
) {
    var id: Long? = null
        private set

    companion object {
        fun read(id: Long): MatchTeamData? {
            return MatchTeams
                .selectAll()
                .where { MatchTeams.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toData()
        }

        fun readByMatchId(matchId: Long): List<MatchTeamData> {
            val matchRef = EntityID(matchId, Matches)
            return MatchTeams
                .selectAll()
                .where { MatchTeams.matchId eq matchRef }
                .map { it.toData() }
        }

        fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return MatchTeams.update({ MatchTeams.id eq id }) { statement ->
                val fetchRow = {
                    MatchTeams
                        .selectAll()
                        .where { MatchTeams.id eq id }
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }

        private fun ResultRow.toData(): MatchTeamData =
            MatchTeamData(
                matchId = this[MatchTeams.matchId].value,
                team = this[MatchTeams.team],
                bedDestroyedAt = this[MatchTeams.bedDestroyedAt],
                eliminatedAt = this[MatchTeams.eliminatedAt],
                finalPlacement = this[MatchTeams.finalPlacement]
            ).apply {
                id = this@toData[MatchTeams.id].value
            }
    }

    fun create(): Boolean {
        val newId = MatchTeams.insertAndGetId { statement ->
            statement[MatchTeams.matchId] = EntityID(this@MatchTeamData.matchId, Matches)
            statement[MatchTeams.team] = this@MatchTeamData.team
            statement[MatchTeams.bedDestroyedAt] = this@MatchTeamData.bedDestroyedAt
            statement[MatchTeams.eliminatedAt] = this@MatchTeamData.eliminatedAt
            statement[MatchTeams.finalPlacement] = this@MatchTeamData.finalPlacement
        }
        this@MatchTeamData.id = newId.value
        return true
    }

    fun update(builder: (UpdateBuilder<Int>.(MatchTeamData) -> Unit)? = null): Boolean {
        val teamId = id ?: return false
        return MatchTeams.update({ MatchTeams.id eq teamId }) { statement ->
            if (builder == null) {
                statement[MatchTeams.team] = this@MatchTeamData.team
                statement[MatchTeams.bedDestroyedAt] = this@MatchTeamData.bedDestroyedAt
                statement[MatchTeams.eliminatedAt] = this@MatchTeamData.eliminatedAt
                statement[MatchTeams.finalPlacement] = this@MatchTeamData.finalPlacement
            } else {
                builder.invoke(statement, this@MatchTeamData)
            }
        } > 0
    }

    fun delete(): Boolean {
        val teamId = id ?: return false
        return MatchTeams.deleteWhere { MatchTeams.id eq teamId } > 0
    }
}
