package me.hosairis.matchvault.services.listeners

import de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent
import de.marcely.bedwars.api.event.player.PlayerBuyUpgradeEvent
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import me.hosairis.matchvault.helpers.CoroutineHelper
import me.hosairis.matchvault.helpers.Log
import me.hosairis.matchvault.services.TrackerService
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
            Log.severe("Failed to obtain id to match (${arena.name} | ${arena.maxPlayers})")
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
                            Log.severe("Failed to obtain team (${team.name}) from table (match_teams) from match ($matchId)")
                            return@transaction
                        }
                    val playerId =
                        TrackerService.playerIds[player.uniqueId]
                            ?: PlayerData.read(uuid = player.uniqueId)?.id
                            ?: run {
                                Log.severe("Failed to obtain ID for player ${player.name} | ${player.uniqueId}")
                                return@transaction
                            }

                    UpgradePurchaseData(
                        matchId,
                        playerId,
                        teamData.id!!,
                        upgradeName,
                        upgradeLevel
                    ).also {
                        if (!it.create()) throw IllegalStateException("Failed to create row in table (upgrade_purchases) for player ($player | ${player.uniqueId}) in team (${team.name}) in match ($matchId) for upgrade ($upgradeName | $upgradeLevel)")
                        it.id ?: throw IllegalStateException("Data in table (upgrade_purchases) for player ($player | ${player.uniqueId}) in match ($matchId) for upgrade ($upgradeName | $upgradeLevel) is missing the ID")
                    }
                }
            } catch (ex: Exception) {
                Log.warning("An error occurred while handling upgrade purchase event: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    private fun onShop(event: PlayerBuyInShopEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena ?: return
        val matchId = TrackerService.matchIds[arena] ?: run {
            Log.severe("Failed to obtain id to match (${arena.name} | ${arena.maxPlayers})")
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
                            Log.severe("Failed to obtain team (${team.name}) from table (match_teams) from match ($matchId)")
                            return@transaction
                        }
                    val playerId =
                        TrackerService.playerIds[player.uniqueId]
                            ?: PlayerData.read(uuid = player.uniqueId)?.id
                            ?: run {
                                Log.severe("Failed to obtain ID for player ${player.name} | ${player.uniqueId}")
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
                        if (!it.create()) throw IllegalStateException("Failed to create row in table (shop_purchases) for player ($player | ${player.uniqueId}) in team (${team.name}) in match ($matchId) for purchase ($itemName | $amount)")
                    }
                    purchaseData.id ?: throw IllegalStateException("Data in table (shop_purchases) for player ($player | ${player.uniqueId}) in match ($matchId) for upgrade ($itemName | $amount) is missing the ID")
                }
            } catch (ex: Exception) {
                Log.warning("An error occurred while handling shop purchase event: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}