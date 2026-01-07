package me.hosairis.matchvault.tracking.purchase

import de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent
import de.marcely.bedwars.api.event.player.PlayerBuyUpgradeEvent
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.service.PurchaseService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PurchaseListener: Listener {

    @EventHandler
    fun onUpgrade(event: PlayerBuyUpgradeEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena
        val player = event.player
        val uuid = player.uniqueId
        val playerName = player.name
        val teamName = event.team.name
        val upgradeName = event.upgradeLevel.upgrade.id
        val upgradeLevel = event.upgradeLevel.level
        val timestamp = System.currentTimeMillis()

        CoroutineHelper.runAsync {
            try {
                PurchaseService.createUpgradePurchase(
                    arena = arena,
                    playerUuid = uuid,
                    teamName = teamName,
                    upgradeName = upgradeName,
                    upgradeLevel = upgradeLevel
                )
            } catch (ex: Throwable) {
                Log.warning("UpgradePurchaseEvent: error recording upgrade for $playerName ($uuid) matchId=${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onShop(event: PlayerBuyInShopEvent) {
        if (event.problems.isNotEmpty()) return

        val arena = event.arena ?: return
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
                PurchaseService.createShopPurchase(
                    arena = arena,
                    playerUuid = uuid,
                    teamName = teamName,
                    itemName = itemName,
                    amount = amount,
                    itemType = itemType,
                    openCause = openCause
                )
            } catch (ex: Throwable) {
                Log.warning("ShopPurchaseEvent: error recording shop purchase for $playerName ($uuid) matchId=${arena.name}: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}