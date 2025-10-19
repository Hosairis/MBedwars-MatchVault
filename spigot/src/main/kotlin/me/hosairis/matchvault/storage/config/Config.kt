package me.hosairis.matchvault.storage.config

class Config private constructor() : AbstractConfig("config.yml") {

    companion object {
        private val instance = Config()

        fun init() = instance.init()
        fun reload(): Boolean = instance.reload()

        var SERVER_NAME: String = ""

        var DATABASE_TYPE: String = ""
        var DATABASE_HOST: String = ""
        var DATABASE_PORT: Int = 3306
        var DATABASE_USER: String = ""
        var DATABASE_PASSWORD: String = ""
        var DATABASE_NAME: String = ""
        var DATABASE_PARAMETERS: String = ""

        var ADVANCED_DEBUG: Boolean = false
        var ADVANCED_DATABASE_FLUSH_INTERVAL: Long = 5
        var ADVANCED_DATABASE_FLUSH_DURATION: Long = 50_000_000
        var ADVANCED_DATABASE_TIME_CHECK_STRIDE: Int = 20

        var CONFIG_VERSION: Int = 1
    }

    override fun loadValues() {
        SERVER_NAME = getConfig().getString("server-name")

        DATABASE_TYPE = getConfig().getString("database.type")
        DATABASE_HOST = getConfig().getString("database.host")
        DATABASE_PORT = getConfig().getInt("database.port")
        DATABASE_USER = getConfig().getString("database.user")
        DATABASE_PASSWORD = getConfig().getString("database.password")
        DATABASE_NAME = getConfig().getString("database.database")
        DATABASE_PARAMETERS = getConfig().getString("database.parameters")

        ADVANCED_DEBUG = getConfig().getBoolean("advanced.debug")
        ADVANCED_DATABASE_FLUSH_INTERVAL = getConfig().getLong("advanced.database.flush-interval")
        ADVANCED_DATABASE_FLUSH_DURATION = getConfig().getLong("advanced.database.flush-duration")
        ADVANCED_DATABASE_TIME_CHECK_STRIDE = getConfig().getInt("advanced.database.time-check-stride")

        CONFIG_VERSION = getConfig().getInt("config-version")
    }
}