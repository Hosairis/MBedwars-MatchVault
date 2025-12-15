package me.hosairis.matchvault

import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.util.Log
import me.hosairis.matchvault.storage.database.Db
import me.hosairis.matchvault.storage.database.repo.MatchPlayerRepository
import me.hosairis.matchvault.storage.database.repo.MatchRepository
import me.hosairis.matchvault.storage.database.repo.MatchTeamRepository
import me.hosairis.matchvault.storage.database.repo.PlayerRepository
import me.hosairis.matchvault.storage.database.repo.ShopPurchaseRepository
import me.hosairis.matchvault.storage.database.repo.TimelineMetaRepository
import me.hosairis.matchvault.storage.database.repo.TimelineRepository
import me.hosairis.matchvault.storage.database.repo.UpgradePurchaseRepository
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.storage.database.service.PlayerService
import me.hosairis.matchvault.storage.database.service.PurchaseService
import me.hosairis.matchvault.storage.database.service.TimelineService
import me.hosairis.matchvault.tracking.TrackerCache
import me.hosairis.matchvault.tracking.listeners.match.EliminationsListener
import me.hosairis.matchvault.tracking.listeners.match.MatchRoundListener
import me.hosairis.matchvault.tracking.listeners.player.PlayerSessionListener
import me.hosairis.matchvault.tracking.listeners.player.PlayerStatsListener
import me.hosairis.matchvault.tracking.listeners.purchase.PurchaseListener
import me.hosairis.matchvault.util.MessageHelper
import org.bstats.bukkit.Metrics
import revxrsal.zapper.ZapperJavaPlugin

class MatchVault: ZapperJavaPlugin() {

    companion object {
        lateinit var instance: MatchVault
        lateinit var addon: MatchVaultAddon
    }

    lateinit var playerService: PlayerService
        private set
    lateinit var matchService: MatchService
        private set
    lateinit var purchaseService: PurchaseService
        private set
    lateinit var timelineService: TimelineService
        private set

    private var metrics: Metrics? = null

    override fun onEnable() {
        instance = this

        addon = MatchVaultAddon(this).registerAddon()

        Config.init(addon.dataFolder)
        Db.init()

        // 3) wire services
        wireServices()

        // 4) register listeners/commands
        registerEntrypoints()

        matchService.abortOngoingMatchesOnStartup(Config.values.serverName)

        metrics = Metrics(this, 27239)

        MessageHelper.printSplashScreen()
    }

    override fun onDisable() {
        metrics?.shutdown()
        metrics = null

        CoroutineHelper.cancelAll()
        TrackerCache.clearAll()
        Db.close()
    }

    private fun wireServices() {
        // repos
        val playerRepo = PlayerRepository()
        val matchRepo = MatchRepository()
        val teamRepo = MatchTeamRepository()
        val matchPlayerRepo = MatchPlayerRepository()
        val shopRepo = ShopPurchaseRepository()
        val upgradeRepo = UpgradePurchaseRepository()
        val timelineRepo = TimelineRepository()
        val timelineMetaRepo = TimelineMetaRepository()

        // services
        playerService = PlayerService(playerRepo)

        matchService = MatchService(
            matchRepo = matchRepo,
            teamRepo = teamRepo,
            matchPlayerRepo = matchPlayerRepo,
            playerRepo = playerRepo
        )

        purchaseService = PurchaseService(
            shopRepo = shopRepo,
            upgradeRepo = upgradeRepo
        )

        timelineService = TimelineService(
            timelineRepo = timelineRepo,
            metaRepo = timelineMetaRepo
        )
    }

    private fun registerEntrypoints() {
        val pm = server.pluginManager

        // tracking listeners (inject services)
        pm.registerEvents(PlayerSessionListener(playerService), this)
        pm.registerEvents(PlayerStatsListener(matchService, playerService), this)
        pm.registerEvents(MatchRoundListener(matchService), this)
        pm.registerEvents(EliminationsListener(matchService), this)
        pm.registerEvents(PurchaseListener(purchaseService, playerService, matchService), this)

        // command wiring (example — adjust to your command framework)
//         getCommand("matchhistory")?.setExecutor(MatchHistoryCMD())
    }
}