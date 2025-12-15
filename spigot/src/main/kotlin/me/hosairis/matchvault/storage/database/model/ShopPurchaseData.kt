package me.hosairis.matchvault.storage.database.model

import de.marcely.bedwars.api.game.shop.ShopOpenCause
import de.marcely.bedwars.api.game.shop.product.ShopProductType

data class ShopPurchaseData(
    val id: Long? = null,
    val matchId: Long,
    val playerId: Long,
    val teamId: Long,
    val item: String,
    val amount: Int,
    val itemType: ShopProductType,
    val openCause: ShopOpenCause
)
