package me.hosairis.matchvault.storage.database.data

import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.MatchStatus
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

data class MatchData(
    var arenaName: String,
    var mode: Int,
    var startedAt: Long,
    var endedAt: Long? = null,
    var duration: Long? = null,
    var status: MatchStatus = MatchStatus.ONGOING,
    var isTie: Boolean = false,
    var winnerTeamId: Long? = null,
    var server: String = Config.SERVER_NAME
) {
    var id: Long? = null
        private set

    var teams: MutableList<MatchTeamData> = mutableListOf()
        private set

    var players: MutableList<MatchPlayerData> = mutableListOf()
        private set

    var shopPurchases: MutableList<ShopPurchaseData> = mutableListOf()
        private set

    var upgradePurchases: MutableList<UpgradePurchaseData> = mutableListOf()
        private set

    var timelines: MutableList<TimelineData> = mutableListOf()
        private set

    companion object {
        fun read(id: Long): MatchData? {
            return Matches
                .selectAll()
                .where { Matches.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let { row ->
                    MatchData(
                        arenaName = row[Matches.arenaName],
                        mode = row[Matches.mode],
                        startedAt = row[Matches.startedAt],
                        endedAt = row[Matches.endedAt],
                        duration = row[Matches.duration],
                        status = row[Matches.status],
                        isTie = row[Matches.isTie],
                        winnerTeamId = row[Matches.winnerTeamId]?.value,
                        server = row[Matches.server]
                    ).apply {
                        this.id = row[Matches.id].value
                    }
                }
        }

        fun update(
            id: Long,
            builder: UpdateBuilder<Int>.(fetchRow: () -> ResultRow?) -> Unit
        ): Boolean {
            return Matches.update({ Matches.id eq id }) { statement ->
                val fetchRow = {
                    Matches
                        .selectAll()
                        .where { Matches.id eq id }
                        .forUpdate()
                        .limit(1)
                        .firstOrNull()
                }
                builder(statement, fetchRow)
            } > 0
        }
    }

    fun create(): Boolean {
        val newId = Matches.insertAndGetId { statement ->
            statement[Matches.arenaName] = this@MatchData.arenaName
            statement[Matches.mode] = this@MatchData.mode
            statement[Matches.startedAt] = this@MatchData.startedAt
            statement[Matches.endedAt] = this@MatchData.endedAt
            statement[Matches.duration] = this@MatchData.duration
            statement[Matches.status] = this@MatchData.status
            statement[Matches.isTie] = this@MatchData.isTie
            statement[Matches.winnerTeamId] = this@MatchData.winnerTeamId?.let { EntityID(it, MatchTeams) }
            statement[Matches.server] = this@MatchData.server
        }
        this@MatchData.id = newId.value
        return true
    }

    fun update(builder: (UpdateBuilder<Int>.(MatchData) -> Unit)? = null): Boolean {
        val matchId = id ?: return false
        return Matches.update({ Matches.id eq matchId }) { statement ->
            if (builder == null) {
                statement[Matches.arenaName] = this@MatchData.arenaName
                statement[Matches.mode] = this@MatchData.mode
                statement[Matches.startedAt] = this@MatchData.startedAt
                statement[Matches.endedAt] = this@MatchData.endedAt
                statement[Matches.duration] = this@MatchData.duration
                statement[Matches.status] = this@MatchData.status
                statement[Matches.isTie] = this@MatchData.isTie
                statement[Matches.winnerTeamId] = this@MatchData.winnerTeamId?.let { EntityID(it, MatchTeams) }
                statement[Matches.server] = this@MatchData.server
            } else {
                builder.invoke(statement, this@MatchData)
            }
        } > 0
    }

    fun delete(): Boolean {
        val matchId = id ?: return false
        return Matches.deleteWhere { Matches.id eq matchId } > 0
    }

    fun loadTeams(){
        val matchId = id ?: return
        teams.clear()
        teams.addAll(MatchTeamData.readByMatchId(matchId))
    }

    fun loadPlayers() {
        val matchId = id ?: return
        players.clear()
        players.addAll(MatchPlayerData.readByMatchId(matchId))
    }

    fun loadShopPurchases() {
        val matchId = id ?: return
        shopPurchases.clear()
        shopPurchases.addAll(ShopPurchaseData.readByMatchId(matchId))
    }

    fun loadUpgradePurchases() {
        val matchId = id ?: return
        upgradePurchases.clear()
        upgradePurchases.addAll(UpgradePurchaseData.readByMatchId(matchId))
    }

    fun loadTimelines() {
        val matchId = id ?: return
        timelines.clear()
        timelines.addAll(TimelineData.readByMatchId(matchId))
    }
}
