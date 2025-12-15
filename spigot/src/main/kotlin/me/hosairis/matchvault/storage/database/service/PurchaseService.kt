package me.hosairis.matchvault.storage.database.service

import me.hosairis.matchvault.storage.database.model.ShopPurchaseData
import me.hosairis.matchvault.storage.database.model.UpgradePurchaseData
import me.hosairis.matchvault.storage.database.repo.ShopPurchaseRepository
import me.hosairis.matchvault.storage.database.repo.UpgradePurchaseRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class PurchaseService(
    private val shopRepo: ShopPurchaseRepository,
    private val upgradeRepo: UpgradePurchaseRepository
) {

    // ---- Shop purchases ----

    fun createShopPurchase(data: ShopPurchaseData): Long = transaction {
        shopRepo.create(data.copy(id = null))
    }

    fun readShopPurchase(id: Long): ShopPurchaseData? = transaction {
        shopRepo.read(id)
    }

    fun readShopPurchasesByMatchId(matchId: Long): List<ShopPurchaseData> = transaction {
        shopRepo.readByMatchId(matchId)
    }

    fun readShopPurchasesByPlayerId(playerId: Long): List<ShopPurchaseData> = transaction {
        shopRepo.readByPlayerId(playerId)
    }

    fun deleteShopPurchase(id: Long): Boolean = transaction {
        shopRepo.delete(id)
    }

    // ---- Upgrade purchases ----

    fun createUpgradePurchase(data: UpgradePurchaseData): Long = transaction {
        upgradeRepo.create(data.copy(id = null))
    }

    fun readUpgradePurchase(id: Long): UpgradePurchaseData? = transaction {
        upgradeRepo.read(id)
    }

    fun readUpgradePurchasesByMatchId(matchId: Long): List<UpgradePurchaseData> = transaction {
        upgradeRepo.readByMatchId(matchId)
    }

    fun readUpgradePurchasesByBuyerId(buyerId: Long): List<UpgradePurchaseData> = transaction {
        upgradeRepo.readByPlayerId(buyerId)
    }

    fun deleteUpgradePurchase(id: Long): Boolean = transaction {
        upgradeRepo.delete(id)
    }
}
