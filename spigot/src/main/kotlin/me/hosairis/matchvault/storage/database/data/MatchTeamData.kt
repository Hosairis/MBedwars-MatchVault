package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
        suspend fun read(id: Long): MatchTeamData? = withContext(Dispatchers.IO) {
            transaction {
                MatchTeams
                    .selectAll()
                    .where { MatchTeams.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toData()
            }
        }

        suspend fun readByMatchId(matchId: Long): List<MatchTeamData> = withContext(Dispatchers.IO) {
            transaction {
                val matchRef = EntityID(matchId, Matches)
                MatchTeams
                    .selectAll()
                    .where { MatchTeams.matchId eq matchRef }
                    .map { it.toData() }
            }
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

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
                val newId = MatchTeams.insertAndGetId { statement ->
                    statement[MatchTeams.matchId] = EntityID(this@MatchTeamData.matchId, Matches)
                    statement[MatchTeams.team] = this@MatchTeamData.team
                    statement[MatchTeams.bedDestroyedAt] = this@MatchTeamData.bedDestroyedAt
                    statement[MatchTeams.eliminatedAt] = this@MatchTeamData.eliminatedAt
                    statement[MatchTeams.finalPlacement] = this@MatchTeamData.finalPlacement
                }
                this@MatchTeamData.id = newId.value
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(builder: (UpdateBuilder<Int>.(MatchTeamData) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val teamId = id ?: return@withContext false
        transaction {
            try {
                MatchTeams.update({ MatchTeams.id eq teamId }) { statement ->
                    if (builder == null) {
                        statement[MatchTeams.team] = this@MatchTeamData.team
                        statement[MatchTeams.bedDestroyedAt] = this@MatchTeamData.bedDestroyedAt
                        statement[MatchTeams.eliminatedAt] = this@MatchTeamData.eliminatedAt
                        statement[MatchTeams.finalPlacement] = this@MatchTeamData.finalPlacement
                    } else {
                        builder.invoke(statement, this@MatchTeamData)
                    }
                } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val teamId = id ?: return@withContext false
        transaction {
            try {
                MatchTeams.deleteWhere { MatchTeams.id eq teamId } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }
}
