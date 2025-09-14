package me.hosairis.matchvault

import org.bstats.bukkit.Metrics
import revxrsal.zapper.ZapperJavaPlugin

class MatchVault: ZapperJavaPlugin() {
    
    companion object {
        private lateinit var instance: MatchVault
        private lateinit var metrics: Metrics

        fun getInst(): MatchVault {
            return instance
        }
    }

    // Called when the plugin is enabled (initialize resources, register events/commands).
    override fun onEnable() {
        instance = this

        metrics = Metrics(getInst(), 27239)
    }

    // Called when the plugin is disabled (cleanup resources, save data).
    override fun onDisable() {
        metrics.shutdown()
    }
}