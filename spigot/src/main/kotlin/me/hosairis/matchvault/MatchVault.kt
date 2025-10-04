package me.hosairis.matchvault

import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.Db
import org.bstats.bukkit.Metrics
import revxrsal.zapper.ZapperJavaPlugin

class MatchVault: ZapperJavaPlugin() {
    companion object {
        private lateinit var instance: MatchVault
        private lateinit var addon: MatchVaultAddon
        private lateinit var metrics: Metrics

        fun getInst(): MatchVault {
            return instance
        }

        fun getAddon(): MatchVaultAddon {
            return addon
        }
    }

    override fun onEnable() {
        instance = this
        metrics = Metrics(getInst(), 27239)

        addon = MatchVaultAddon(this).registerAddon()

        Config.init()
        Db.init()

        addon.registerModules()
    }

    override fun onDisable() {
        metrics.shutdown()
        Db.close()
        addon.unregisterModules()
    }
}