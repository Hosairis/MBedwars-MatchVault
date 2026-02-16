package me.hosairis.matchvault

import de.marcely.bedwars.api.BedwarsAPI
import me.hosairis.matchvault.command.MatchHistoryCMD
import me.hosairis.matchvault.command.MatchVaultCMD
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
import me.hosairis.matchvault.util.Log
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
        // tracking listeners
        server.pluginManager.registerEvents(PlayerSessionListener(), this)
        server.pluginManager.registerEvents(PlayerStatsListener(), this)
        server.pluginManager.registerEvents(MatchRoundListener(), this)
        server.pluginManager.registerEvents(EliminationsListener(), this)
        server.pluginManager.registerEvents(PurchaseListener(), this)

        // command wiring
        getCommand("matchvault").executor = MatchVaultCMD()

        val historyCmd = BedwarsAPI.getRootCommandsCollection().addCommand("matches") ?: run {
            Log.severe("Command /bw matches already exists (or couldn't be added).")
            return
        }
        historyCmd.usage = "[arg1]"
        historyCmd.permission = "matchvault.commands.history"
        historyCmd.setAliases("matchlist", "matchhistory")
        historyCmd.handler = MatchHistoryCMD()
    }
}