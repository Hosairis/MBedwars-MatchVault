package me.hosairis.matchvault.storage.config

class Config private constructor() : AbstractConfig("config.yml") {

    companion object {
        private val instance = Config()

        fun init() = instance.init()
        fun reload(): Boolean = instance.reload()

        var SERVER_NAME: String = ""
        var TRACK_TIMELINES: Boolean = true
        var DATABASE_TYPE: String = ""
        var DATABASE_HOST: String = ""
        var DATABASE_PORT: Int = 3306
        var DATABASE_USER: String = ""
        var DATABASE_PASSWORD: String = ""
        var DATABASE_NAME: String = ""
        var DATABASE_PARAMETERS: String = ""

        var CONFIG_VERSION: Int = 1
    }

    override fun loadValues() {
        SERVER_NAME = getConfig().getString("server-name")
        TRACK_TIMELINES = getConfig().getBoolean("track-timelines")
        DATABASE_TYPE = getConfig().getString("database.type")
        DATABASE_HOST = getConfig().getString("database.host")
        DATABASE_PORT = getConfig().getInt("database.port")
        DATABASE_USER = getConfig().getString("database.user")
        DATABASE_PASSWORD = getConfig().getString("database.password")
        DATABASE_NAME = getConfig().getString("database.database")
        DATABASE_PARAMETERS = getConfig().getString("database.parameters")

        CONFIG_VERSION = getConfig().getInt("config-version")
    }
}