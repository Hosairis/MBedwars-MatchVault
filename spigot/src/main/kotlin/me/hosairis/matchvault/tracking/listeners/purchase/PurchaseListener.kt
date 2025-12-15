package me.hosairis.matchvault.tracking.listeners.purchase

import de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent
import de.marcely.bedwars.api.event.player.PlayerBuyUpgradeEvent
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.model.ShopPurchaseData
import me.hosairis.matchvault.storage.database.model.UpgradePurchaseData
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.storage.database.service.PlayerService
import me.hosairis.matchvault.storage.database.service.PurchaseService
import me.hosairis.matchvault.tracking.TrackerCache
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PurchaseListener(
    private val purchaseService: PurchaseService,
    private val playerService: PlayerService,
    private val matchService: MatchService
) : Listener {

    @EventHandler
    fun onUpgrade(event: PlayerBuyUpgradeEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena
        val matchId = TrackerCache.matchIds[arena] ?: run {
            Log.severe("UpgradePurchaseEvent: missing match ID for arena ${arena.name}")
            return
        }

        val player = event.player
        val uuid = player.uniqueId
        val playerName = player.name
        val teamName = event.team.name

        val upgradeName = event.upgradeLevel.upgrade.id
        val upgradeLevel = event.upgradeLevel.level
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                val playerId = TrackerCache.playerIds[uuid]
                    ?: playerService.readByUuid(uuid)?.id
                    ?: run {
                        Log.severe("UpgradePurchaseEvent: missing playerId for $playerName ($uuid)")
                        return@runAsync
                    }

                val teamId = matchService.readTeamByMatchIdAndTeam(matchId, teamName) ?: run {
                    Log.severe("UpgradePurchaseEvent: team $teamName not found for matchId $matchId")
                    return@runAsync
                }

                purchaseService.createUpgradePurchase(
                    UpgradePurchaseData(
                        id = null,
                        matchId = matchId,
                        playerId = playerId,
                        teamId = teamId,
                        upgrade = upgradeName,
                        level = upgradeLevel
                    )
                )
            } catch (ex: Throwable) {
                Log.warning("UpgradePurchaseEvent: error recording upgrade for $playerName ($uuid) matchId=$matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onShop(event: PlayerBuyInShopEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena ?: return
        val matchId = TrackerCache.matchIds[arena] ?: run {
            Log.severe("ShopPurchaseEvent: missing match ID for arena ${arena.name}")
            return
        }

        val player = event.player
        val uuid = player.uniqueId
        val playerName = player.name

        val team = arena.getPlayerTeam(player) ?: return
        val teamName = team.name

        val itemName = event.item.displayName
        val openCause = event.openCause

        var amount = 0
        var itemType = ShopProductType.ITEM
        for (product in event.item.products) {
            itemType = product.type
            if (product.isAutoWear) {
                amount = event.multiplier
                break
            }
            amount += product.amount * event.multiplier
        }

        CoroutineHelper.runAsync {
            try {
                val playerId = TrackerCache.playerIds[uuid]
                    ?: playerService.readByUuid(uuid)?.id
                    ?: run {
                        Log.severe("ShopPurchaseEvent: missing playerId for $playerName ($uuid)")
                        return@runAsync
                    }

                val teamId = matchService.readTeamByMatchIdAndTeam(matchId, teamName) ?: run {
                    Log.severe("ShopPurchaseEvent: team $teamName not found for matchId $matchId")
                    return@runAsync
                }

                purchaseService.createShopPurchase(
                    ShopPurchaseData(
                        id = null,
                        matchId = matchId,
                        playerId = playerId,
                        teamId = teamId,
                        item = itemName,
                        amount = amount,
                        itemType = itemType,
                        openCause = openCause
                    )
                )
            } catch (ex: Throwable) {
                Log.warning("ShopPurchaseEvent: error recording shop purchase for $playerName ($uuid) matchId=$matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}