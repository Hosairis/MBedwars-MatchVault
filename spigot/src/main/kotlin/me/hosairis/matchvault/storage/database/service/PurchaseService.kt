package me.hosairis.matchvault.storage.database.service

import de.marcely.bedwars.api.arena.Arena
import de.marcely.bedwars.api.game.shop.ShopOpenCause
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import me.hosairis.matchvault.storage.database.model.ShopPurchaseData
import me.hosairis.matchvault.storage.database.model.UpgradePurchaseData
import me.hosairis.matchvault.storage.database.repo.ShopPurchaseRepository
import me.hosairis.matchvault.storage.database.repo.UpgradePurchaseRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

object PurchaseService {
    private val shopRepo = ShopPurchaseRepository()
    private val upgradeRepo = UpgradePurchaseRepository()

    // ---- Shop purchases ----

    fun createShopPurchase(
        arena: Arena,
        playerUuid: UUID,
        teamName: String,
        itemName: String,
        amount: Int,
        itemType: ShopProductType,
        openCause: ShopOpenCause
    ): Long = transaction {
        val matchId = MatchService.readMatchId(arena) ?: return@transaction -1
        val playerId = PlayerService.readIdByUuid(playerUuid) ?: return@transaction -1
        val teamId = MatchService.readTeamByMatchIdAndTeam(matchId, teamName) ?: return@transaction -1

        val shopPurchaseData = ShopPurchaseData(
            matchId = matchId,
            playerId = playerId,
            teamId = teamId,
            item = itemName,
            amount = amount,
            itemType = itemType,
            openCause = openCause
        )
        shopRepo.create(shopPurchaseData)
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

    fun createUpgradePurchase(
        arena: Arena,
        playerUuid: UUID,
        teamName: String,
        upgradeName: String,
        upgradeLevel: Int
    ): Long = transaction {
        val matchId = MatchService.readMatchId(arena) ?: return@transaction -1
        val playerId = PlayerService.readIdByUuid(playerUuid) ?: return@transaction -1
        val teamId = MatchService.readTeamByMatchIdAndTeam(matchId, teamName) ?: return@transaction -1

        val upgradePurchaseData = UpgradePurchaseData(
            matchId = matchId,
            playerId = playerId,
            teamId = teamId,
            upgrade = upgradeName,
            level = upgradeLevel
        )
        upgradeRepo.create(upgradePurchaseData)
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
