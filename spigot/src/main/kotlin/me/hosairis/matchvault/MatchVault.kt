package me.hosairis.matchvault

import me.hosairis.matchvault.command.HistoryCMD
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.config.Messages
import me.hosairis.matchvault.util.CoroutineHelper
import me.hosairis.matchvault.storage.database.Db
import me.hosairis.matchvault.storage.database.service.MatchService
import me.hosairis.matchvault.tracking.match.EliminationsListener
import me.hosairis.matchvault.tracking.match.MatchRoundListener
import me.hosairis.matchvault.tracking.player.PlayerSessionListener
import me.hosairis.matchvault.tracking.player.PlayerStatsListener
import me.hosairis.matchvault.tracking.purchase.PurchaseListener
import me.hosairis.matchvault.util.MessageHelper
import org.bstats.bukkit.Metrics
import revxrsal.zapper.ZapperJavaPlugin

class MatchVault: ZapperJavaPlugin() {

    companion object {
        lateinit var instance: MatchVault
        lateinit var addon: MatchVaultAddon
    }

    private var metrics: Metrics? = null

    override fun onEnable() {
        instance = this

        addon = MatchVaultAddon(this).registerAddon()

        Config.init(addon.dataFolder)
        Messages.init(addon.dataFolder)
        Db.init()

        // 4) register listeners/commands
        registerEntrypoints()

        MatchService.abortOngoingMatchesOnStartup(Config.values.serverName)

        metrics = Metrics(this, 27239)

        MessageHelper.printSplashScreen()
    }

    override fun onDisable() {
        metrics?.shutdown()
        metrics = null

        CoroutineHelper.cancelAll()

        Db.close()
    }

    private fun registerEntrypoints() {
        val pm = server.pluginManager

        // tracking listeners (inject services)
        pm.registerEvents(PlayerSessionListener(), this)
        pm.registerEvents(PlayerStatsListener(), this)
        pm.registerEvents(MatchRoundListener(), this)
        pm.registerEvents(EliminationsListener(), this)
        pm.registerEvents(PurchaseListener(), this)

        // command wiring
        getCommand("matchhistory").executor = HistoryCMD()
    }
}