package me.hosairis.matchvault.sotrage.config

class Config private constructor() : AbstractConfig("config.yml") {

    companion object {
        private val instance = Config()

        fun init() = instance.init()
        fun reload(): Boolean = instance.reload()

        var CONFIG_VERSION: Int = 1
    }

    override fun loadValues() {
        CONFIG_VERSION = getConfig().getInt("config-version", CONFIG_VERSION)
    }
}