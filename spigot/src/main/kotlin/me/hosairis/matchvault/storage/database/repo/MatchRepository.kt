package me.hosairis.matchvault.storage.database.repo

import me.hosairis.matchvault.storage.database.enums.MatchStatus
import me.hosairis.matchvault.storage.database.model.MatchData
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class MatchRepository {

    fun create(data: MatchData): Long {
        return Matches.insertAndGetId { st ->
            st[arenaName] = data.arenaName
            st[mode] = data.mode
            st[startedAt] = data.startedAt
            st[endedAt] = data.endedAt
            st[duration] = data.duration
            st[status] = data.status
            st[isTie] = data.isTie
            st[winnerTeamId] = data.winnerTeamId?.let { EntityID(it, MatchTeams) }
            st[server] = data.server
        }.value
    }

    fun update(data: MatchData): Boolean {
        val exists = Matches
            .select(Matches.id)
            .where { Matches.id eq data.id }
            .forUpdate()
            .limit(1)
            .any()

        if (!exists) return false

        return Matches.update({ Matches.id eq data.id }) { st ->
            st[arenaName] = data.arenaName
            st[mode] = data.mode
            st[startedAt] = data.startedAt
            st[endedAt] = data.endedAt
            st[duration] = data.duration
            st[status] = data.status
            st[isTie] = data.isTie
            st[winnerTeamId] = data.winnerTeamId?.let { EntityID(it, MatchTeams) }
            st[server] = data.server
        } > 0
    }

    fun delete(id: Long): Boolean {
        return Matches.deleteWhere { Matches.id eq id } > 0
    }

    fun read(id: Long): MatchData? {
        return Matches
            .selectAll()
            .where { Matches.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toData()
    }

    fun abortOngoingByServer(server: String): Int {
        return Matches.update(
            where = { (Matches.status eq MatchStatus.ONGOING) and (Matches.server eq server) }
        ) {
            it[Matches.status] = MatchStatus.ABORTED
        }
    }

    private fun ResultRow.toData(): MatchData {
        return MatchData(
            id = this[Matches.id].value,
            arenaName = this[Matches.arenaName],
            mode = this[Matches.mode],
            startedAt = this[Matches.startedAt],
            endedAt = this[Matches.endedAt],
            duration = this[Matches.duration],
            status = this[Matches.status],
            isTie = this[Matches.isTie],
            winnerTeamId = this[Matches.winnerTeamId]?.value,
            server = this[Matches.server]
        )
    }
}
