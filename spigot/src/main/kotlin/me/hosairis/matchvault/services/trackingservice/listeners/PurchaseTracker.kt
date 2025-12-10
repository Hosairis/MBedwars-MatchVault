package me.hosairis.matchvault.services.trackingservice.listeners

import de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent
import de.marcely.bedwars.api.event.player.PlayerBuyUpgradeEvent
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.trackingservice.TrackerService
import me.hosairis.matchvault.storage.database.data.MatchTeamData
import me.hosairis.matchvault.storage.database.data.PlayerData
import me.hosairis.matchvault.storage.database.data.ShopPurchaseData
import me.hosairis.matchvault.storage.database.data.UpgradePurchaseData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class PurchaseTracker: Listener {
    @EventHandler
    private fun onUpgrade(event: PlayerBuyUpgradeEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.severe("UpgradePurchaseEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val player = event.player
        val team = event.team
        val upgradeName = event.upgradeLevel.upgrade.id
        val upgradeLevel = event.upgradeLevel.level

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val teamData = MatchTeamData.readByMatchId(matchId)
                        .find { it.team == team.name }
                        ?.takeIf { it.id != null }
                        ?: run {
                            Log.severe("UpgradePurchaseEvent: team ${team.name} not found for matchId $matchId in match_teams")
                            return@transaction
                        }
                    val playerId =
                        TrackerService.playerIds[player.uniqueId]
                            ?: PlayerData.read(uuid = player.uniqueId)?.id
                            ?: run {
                                Log.severe("UpgradePurchaseEvent: missing player ID for ${player.name} (${player.uniqueId})")
                                return@transaction
                            }

                    UpgradePurchaseData(
                        matchId,
                        playerId,
                        teamData.id!!,
                        upgradeName,
                        upgradeLevel
                    ).also {
                        if (!it.create()) throw IllegalStateException("UpgradePurchaseEvent: failed to create upgrade_purchases row for ${player.name} (${player.uniqueId}) on team ${team.name} in matchId $matchId (upgrade=$upgradeName level=$upgradeLevel)")
                        it.id ?: throw IllegalStateException("UpgradePurchaseEvent: upgrade_purchases row missing ID for ${player.name} (${player.uniqueId}) in matchId $matchId (upgrade=$upgradeName level=$upgradeLevel)")
                    }
                }
            } catch (ex: Exception) {
                Log.warning("UpgradePurchaseEvent: error recording upgrade purchase for ${player.name} (${player.uniqueId}) in matchId $matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onShop(event: PlayerBuyInShopEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena ?: return
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.severe("ShopPurchaseEvent: missing match ID for arena ${arena.name} (maxPlayers=${arena.maxPlayers})")
            return
        }
        val player = event.player
        val team = arena.getPlayerTeam(player) ?: return
        val itemName = event.item.displayName
        var amount = 0
        var itemType = ShopProductType.ITEM
        for (product in event.item.products) {
            itemType = product.type
            if (product.isAutoWear) {
                // Auto-wear items (armor upgrades) give multiple pieces but should count as one purchase
                amount = event.multiplier
                break
            }
            amount += product.amount * event.multiplier
        }
        val openCause = event.openCause

        CoroutineHelper.runAsync {
            try {
                transaction {
                    val teamData = MatchTeamData.readByMatchId(matchId)
                        .find { it.team == team.name }
                        ?.takeIf { it.id != null }
                        ?: run {
                            Log.severe("ShopPurchaseEvent: team ${team.name} not found for matchId $matchId in match_teams")
                            return@transaction
                        }
                    val playerId =
                        TrackerService.playerIds[player.uniqueId]
                            ?: PlayerData.read(uuid = player.uniqueId)?.id
                            ?: run {
                                Log.severe("ShopPurchaseEvent: missing player ID for ${player.name} (${player.uniqueId})")
                                return@transaction
                            }

                    val purchaseData = ShopPurchaseData(
                        matchId,
                        playerId,
                        teamData.id!!,
                        itemName,
                        amount,
                        itemType,
                        openCause
                    ).also {
                        if (!it.create()) throw IllegalStateException("ShopPurchaseEvent: failed to create shop_purchases row for ${player.name} (${player.uniqueId}) on team ${team.name} in matchId $matchId for purchase ($itemName x$amount)")
                    }
                    purchaseData.id ?: throw IllegalStateException("ShopPurchaseEvent: shop_purchases row missing ID for ${player.name} (${player.uniqueId}) in matchId $matchId for purchase ($itemName x$amount)")
                }
            } catch (ex: Exception) {
                Log.warning("ShopPurchaseEvent: error recording shop purchase for ${player.name} (${player.uniqueId}) in matchId $matchId: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}
