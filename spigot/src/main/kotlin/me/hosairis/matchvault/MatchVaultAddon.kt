package me.hosairis.matchvault

import de.marcely.bedwars.api.BedwarsAddon
import org.bukkit.Bukkit

class MatchVaultAddon(plugin: MatchVault) : BedwarsAddon(plugin) {
    companion object {
        const val MIN_BW_API_VER = 203
        const val MIN_BW_VER = "5.5.3"
    }

    override fun getName(): String {
        return plugin.description.name
    }

    fun registerAddon(): MatchVaultAddon {
        try {
            val apiClass = Class.forName("de.marcely.bedwars.api.BedwarsAPI")
            val apiVersion = apiClass.getMethod("getAPIVersion").invoke(null) as Int

            if (apiVersion < MIN_BW_API_VER) {
                throw IllegalStateException()
            }
        } catch (_: Exception) {
            Bukkit.getLogger().severe("Unsupported MBedwars version detected, Please update to v$MIN_BW_VER")
            Bukkit.getPluginManager().disablePlugin(plugin)
        }

        val registered = this.register()
        if (!registered) {
            Bukkit.getLogger().severe("An error occurred, Please check for duplicate addons and remove them")
            Bukkit.getPluginManager().disablePlugin(plugin)
        }

        return this
    }
}