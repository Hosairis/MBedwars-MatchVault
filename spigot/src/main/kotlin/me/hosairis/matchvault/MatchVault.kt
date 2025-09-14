package me.hosairis.matchvault

import org.bukkit.plugin.java.JavaPlugin

class MatchVault: JavaPlugin() {
    
    companion object {
        private lateinit var instance: MatchVault
//        private lateinit var metrics: Metrics

        fun getInst(): MatchVault {
            return instance
        }
    }

    // Called when the plugin is enabled (initialize resources, register events/commands).
    override fun onEnable() {
        instance = this

//        metrics = Metrics(getInst(), /* bStats ID Here */)
    }

    // Called when the plugin is disabled (cleanup resources, save data).
    override fun onDisable() {
//        metrics.shutdown()
    }
}