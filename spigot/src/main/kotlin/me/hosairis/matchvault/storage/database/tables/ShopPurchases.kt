package me.hosairis.matchvault.storage.database.tables

import de.marcely.bedwars.api.game.shop.ShopOpenCause
import de.marcely.bedwars.api.game.shop.product.ShopProductType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object ShopPurchases : LongIdTable("shop_purchases") {
    val matchId = reference("match_id", Matches, onDelete = ReferenceOption.CASCADE).index()
    val playerId = reference("player_id", Players, onDelete = ReferenceOption.CASCADE).index()
    val teamId = reference("team_id", MatchTeams, onDelete = ReferenceOption.CASCADE).index()
    val item = varchar("item", 64)
    val amount = integer("amount").default(1)
    val itemType = enumerationByName("item_type", 32, ShopProductType::class)
    val openCause = enumerationByName("open_cause", 32, ShopOpenCause::class)
}