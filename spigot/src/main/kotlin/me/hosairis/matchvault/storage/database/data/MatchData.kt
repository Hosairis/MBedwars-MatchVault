package me.hosairis.matchvault.storage.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.MatchStatus
import me.hosairis.matchvault.storage.database.MatchTeams
import me.hosairis.matchvault.storage.database.Matches
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
        suspend fun read(id: Long): MatchData? = withContext(Dispatchers.IO) {
            transaction {
                Matches
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
        }
    }

    suspend fun create(): Boolean = withContext(Dispatchers.IO) {
        transaction {
            try {
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
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun update(): Boolean = withContext(Dispatchers.IO) {
        val matchId = id ?: return@withContext false
        transaction {
            try {
                Matches.update({ Matches.id eq matchId }) { statement ->
                    statement[Matches.arenaName] = this@MatchData.arenaName
                    statement[Matches.mode] = this@MatchData.mode
                    statement[Matches.startedAt] = this@MatchData.startedAt
                    statement[Matches.endedAt] = this@MatchData.endedAt
                    statement[Matches.duration] = this@MatchData.duration
                    statement[Matches.status] = this@MatchData.status
                    statement[Matches.isTie] = this@MatchData.isTie
                    statement[Matches.winnerTeamId] = this@MatchData.winnerTeamId?.let { EntityID(it, MatchTeams) }
                    statement[Matches.server] = this@MatchData.server
                } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        val matchId = id ?: return@withContext false
        transaction {
            try {
                Matches.deleteWhere { Matches.id eq matchId } > 0
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    suspend fun loadTeams(){
        val matchId = id ?: return
        teams.clear()
        teams.addAll(MatchTeamData.readByMatchId(matchId))
    }

    suspend fun loadPlayers() {
        val matchId = id ?: return
        players.clear()
        players.addAll(MatchPlayerData.readByMatchId(matchId))
    }

    suspend fun loadShopPurchases() {
        val matchId = id ?: return
        shopPurchases.clear()
        shopPurchases.addAll(ShopPurchaseData.readByMatchId(matchId))
    }

    suspend fun loadUpgradePurchases() {
        val matchId = id ?: return
        upgradePurchases.clear()
        upgradePurchases.addAll(UpgradePurchaseData.readByMatchId(matchId))
    }

    suspend fun loadTimelines() {
        val matchId = id ?: return
        timelines.clear()
        timelines.addAll(TimelineData.readByMatchId(matchId))
    }
}
