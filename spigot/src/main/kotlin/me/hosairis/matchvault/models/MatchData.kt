//package me.hosairis.matchvault.models
//
//import me.hosairis.matchvault.storage.config.Config
//import me.hosairis.matchvault.storage.database.EventType
//import me.hosairis.matchvault.storage.database.MatchPlayers
//import me.hosairis.matchvault.storage.database.MatchStatus
//import me.hosairis.matchvault.storage.database.MatchTeams
//import me.hosairis.matchvault.storage.database.Matches
//import me.hosairis.matchvault.storage.database.Players
//import me.hosairis.matchvault.storage.database.TimelineMetas
//import me.hosairis.matchvault.storage.database.Timelines
//import me.hosairis.matchvault.storage.database.UpgradePurchases
//import java.util.UUID
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.CopyOnWriteArrayList
//import org.jetbrains.exposed.v1.core.dao.id.EntityID
//import org.jetbrains.exposed.v1.core.eq
//import org.jetbrains.exposed.v1.jdbc.insertAndGetId
//import org.jetbrains.exposed.v1.jdbc.select
//import org.jetbrains.exposed.v1.jdbc.transactions.transaction
//
//data class MatchData(
//    val id: Long,
//    val arenaName: String,
//    val mode: Int,
//    val startedAt: Long,
//    var endedAt: Long? = null,
//    var duration: Long? = null,
//    var status: MatchStatus = MatchStatus.ONGOING,
//    var isTie: Boolean = false,
//    var winnerTeamId: Long? = null,
//    val server: String = Config.SERVER_NAME,
//
//    // Nullable collections for optional loading
//    val teams: MutableMap<Long, TeamData>? = ConcurrentHashMap(),
//    val players: MutableMap<Long, PlayerData>? = ConcurrentHashMap(),
//    val upgrades: MutableList<UpgradePurchase>? = CopyOnWriteArrayList(),
//    val timelines: MutableList<TimelineEvent>? = CopyOnWriteArrayList()
//) {
//    companion object {
//        fun create(
//            arenaName: String,
//            mode: Int,
//            startedAt: Long,
//            endedAt: Long? = null,
//            duration: Long? = null,
//            status: MatchStatus = MatchStatus.ONGOING,
//            isTie: Boolean = false,
//            winnerTeamId: Long? = null,
//            server: String = Config.SERVER_NAME
//        ): MatchData = transaction {
//            val winnerRef = winnerTeamId?.let { EntityID(it, MatchTeams) }
//            val matchId = Matches.insertAndGetId {
//                it[Matches.arenaName] = arenaName
//                it[Matches.mode] = mode
//                it[Matches.startedAt] = startedAt
//                it[Matches.endedAt] = endedAt
//                it[Matches.duration] = duration
//                it[Matches.status] = status
//                it[Matches.isTie] = isTie
//                it[Matches.winnerTeamId] = winnerRef
//                it[Matches.server] = server
//            }.value
//
//            MatchData(
//                id = matchId,
//                arenaName = arenaName,
//                mode = mode,
//                startedAt = startedAt,
//                endedAt = endedAt,
//                duration = duration,
//                status = status,
//                isTie = isTie,
//                winnerTeamId = winnerTeamId,
//                server = server
//            )
//        }
//
//        fun from(
//            id: Long,
//            loadTeams: Boolean = true,
//            loadPlayers: Boolean = true,
//            loadUpgrades: Boolean = true,
//            loadTimelines: Boolean = true
//        ): MatchData? = transaction {
//            val row = Matches
//                .select(Matches.id eq id)
//                .limit(1)
//                .firstOrNull()
//                ?: return@transaction null
//
//            val match = MatchData(
//                id = row[Matches.id].value,
//                arenaName = row[Matches.arenaName],
//                mode = row[Matches.mode],
//                startedAt = row[Matches.startedAt],
//                endedAt = row[Matches.endedAt],
//                duration = row[Matches.duration],
//                status = row[Matches.status],
//                isTie = row[Matches.isTie],
//                winnerTeamId = row[Matches.winnerTeamId]?.value,
//                server = row[Matches.server],
//                teams = if (loadTeams) ConcurrentHashMap() else null,
//                players = if (loadPlayers) ConcurrentHashMap() else null,
//                upgrades = if (loadUpgrades) CopyOnWriteArrayList() else null,
//                timelines = if (loadTimelines) CopyOnWriteArrayList() else null
//            )
//
//            if (loadTeams) {
//                val matchRef = EntityID(id, Matches)
//                MatchTeams
//                    .select(MatchTeams.matchId eq matchRef)
//                    .forEach { teamRow ->
//                        val teamData = TeamData(
//                            id = teamRow[MatchTeams.id].value,
//                            team = teamRow[MatchTeams.team],
//                            bedDestroyedAt = teamRow[MatchTeams.bedDestroyedAt],
//                            eliminatedAt = teamRow[MatchTeams.eliminatedAt],
//                            finalPlacement = teamRow[MatchTeams.finalPlacement]
//                        )
//                        match.teams?.put(teamData.id, teamData)
//                    }
//            }
//
//            if (loadPlayers) {
//                val matchRef = EntityID(id, Matches)
//                MatchPlayers
//                    .select(MatchPlayers.matchId eq matchRef)
//                    .forEach { rowPlayer ->
//                        val playerRef = rowPlayer[MatchPlayers.playerId]
//                        val playerRow = Players
//                            .select(Players.id eq playerRef)
//                            .limit(1)
//                            .firstOrNull()
//                            ?: return@forEach
//
//                        val playerData = PlayerData(
//                            id = playerRow[Players.id].value,
//                            name = playerRow[Players.name],
//                            uuid = playerRow[Players.uuid],
//                            firstSeen = playerRow[Players.firstSeen],
//                            lastSeen = playerRow[Players.lastSeen]
//                        )
//                        match.players?.put(playerData.id, playerData)
//                    }
//            }
//
//            if (loadUpgrades) {
//                val matchRef = EntityID(id, Matches)
//                UpgradePurchases
//                    .select(UpgradePurchases.matchId eq matchRef)
//                    .forEach { upgradeRow ->
//                        val upgrade = UpgradePurchase(
//                            id = upgradeRow[UpgradePurchases.id].value,
//                            matchId = upgradeRow[UpgradePurchases.matchId].value,
//                            buyerId = upgradeRow[UpgradePurchases.buyerId].value,
//                            teamId = upgradeRow[UpgradePurchases.teamId].value,
//                            upgrade = upgradeRow[UpgradePurchases.upgrade],
//                            level = upgradeRow[UpgradePurchases.level]
//                        )
//                        match.upgrades?.add(upgrade)
//                    }
//            }
//
//            if (loadTimelines) {
//                val matchRef = EntityID(id, Matches)
//                Timelines
//                    .select(Timelines.matchId eq matchRef)
//                    .toList()
//                    .sortedBy { it[Timelines.timestamp] }
//                    .forEach { timelineRow ->
//                        val timelineId = timelineRow[Timelines.id].value
//                        val metas = TimelineMetas
//                            .select(TimelineMetas.timelineId eq EntityID(timelineId, Timelines))
//                            .associate { metaRow ->
//                                metaRow[TimelineMetas.key] to metaRow[TimelineMetas.value]
//                            }
//                        val event = TimelineEvent(
//                            id = timelineId,
//                            playerId = timelineRow[Timelines.playerId]?.value,
//                            targetId = timelineRow[Timelines.targetId]?.value,
//                            teamId = timelineRow[Timelines.teamId]?.value,
//                            timestamp = timelineRow[Timelines.timestamp],
//                            type = timelineRow[Timelines.type],
//                            metas = metas
//                        )
//                        match.timelines?.add(event)
//                    }
//            }
//
//            match
//        }
//    }
//}
//
//data class TeamData(
//    val id: Long,
//    val team: String,
//    val bedDestroyedAt: Long?,
//    val eliminatedAt: Long?,
//    val finalPlacement: Int?
//)
//
//data class PlayerData(
//    val id: Long,
//    val name: String,
//    val uuid: UUID,
//    val firstSeen: Long,
//    val lastSeen: Long
//)
//
//data class UpgradePurchase(
//    val id: Long,
//    val matchId: Long,
//    val buyerId: Long,
//    val teamId: Long,
//    val upgrade: String,
//    val level: Int
//)
//
//data class TimelineEvent(
//    val id: Long,
//    val playerId: Long?,
//    val targetId: Long?,
//    val teamId: Long?,
//    val timestamp: Long,
//    val type: EventType,
//    val metas: Map<String, String> = ConcurrentHashMap()
//)
