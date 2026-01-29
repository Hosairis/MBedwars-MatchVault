package me.hosairis.matchvault.storage.config

import dev.dejvokep.boostedyaml.YamlDocument

object Config : AbstractConfig("config.yml") {

    data class Values(
        val serverName: String,

        val databaseType: String,
        val databaseHost: String,
        val databasePort: Int,
        val databaseUser: String,
        val databasePassword: String,
        val databaseName: String,
        val databaseParameters: String,

        val configVersion: Int
    )

    @Volatile
    var values: Values = Values(
        serverName = "unknown",

        databaseType = "H2",
        databaseHost = "localhost",
        databasePort = 3306,
        databaseUser = "root",
        databasePassword = "Admin@123",
        databaseName = "matchvault",
        databaseParameters = "?useSSL=false&allowMultiQueries=true",

        configVersion =  1
    )
        private set

    override fun loadValues(doc: YamlDocument) {
        values = Values(
            serverName = doc.getString("server-name"),

            databaseType = doc.getString("database.type"),
            databaseHost = doc.getString("database.host"),
            databasePort = doc.getInt("database.port"),
            databaseUser = doc.getString("database.user"),
            databasePassword = doc.getString("database.password"),
            databaseName = doc.getString("database.name"),
            databaseParameters = doc.getString("database.parameters"),

            configVersion = doc.getInt("config-version")
        )
    }
}
