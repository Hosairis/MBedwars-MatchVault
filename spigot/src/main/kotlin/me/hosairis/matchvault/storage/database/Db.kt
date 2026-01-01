package me.hosairis.matchvault.storage.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config
import me.hosairis.matchvault.storage.database.tables.MatchPlayers
import me.hosairis.matchvault.storage.database.tables.MatchTeams
import me.hosairis.matchvault.storage.database.tables.Matches
import me.hosairis.matchvault.storage.database.tables.Players
import me.hosairis.matchvault.storage.database.tables.ShopPurchases
import me.hosairis.matchvault.storage.database.tables.MatchEventMetas
import me.hosairis.matchvault.storage.database.tables.MatchEvents
import me.hosairis.matchvault.storage.database.tables.UpgradePurchases
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.util.Locale
import javax.sql.DataSource

object Db {

    lateinit var dataSource: DataSource
        private set

    fun init() {
        val dbType = Config.values.databaseType.lowercase(Locale.ROOT).takeIf {
            it in listOf("h2", "sqlite", "mysql", "mariadb")
        } ?: run {
            Bukkit.getLogger().severe("Unsupported database type: ${Config.values.databaseType}")
            Bukkit.getPluginManager().disablePlugin(MatchVault.instance)
            return
        }
        val hikariCfg = HikariConfig().apply {
            poolName = "MatchVault-HikariPool"
            isAutoCommit = false
            leakDetectionThreshold = 5000
            connectionTimeout = 5000
            when (dbType) {
                "sqlite" -> {
                    driverClassName = "org.sqlite.JDBC"
                    maximumPoolSize = 1
                    minimumIdle = 1
                    val dbFile = File(MatchVault.addon.dataFolder, "data.sqlite.db")
                    dbFile.parentFile.mkdirs()
                    jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
                    connectionInitSql = "PRAGMA foreign_keys=ON; PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL;"
                }
                "h2" -> {
                    driverClassName = "org.h2.Driver"
                    maximumPoolSize = 4
                    minimumIdle = 1
                    val dbFile = File(MatchVault.addon.dataFolder, "data.h2")
                    dbFile.parentFile.mkdirs()
                    jdbcUrl = "jdbc:h2:file:${dbFile.absolutePath};MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;DATABASE_TO_UPPER=FALSE;LOCK_TIMEOUT=10000;TRACE_LEVEL_FILE=0"
                }
                "mysql", "mariadb" -> {
                    driverClassName = "org.mariadb.jdbc.Driver"
                    username = Config.values.databaseUser
                    password = Config.values.databasePassword
                    jdbcUrl = "jdbc:mariadb://${Config.values.databaseHost}:${Config.values.databasePort}/${Config.values.databaseName}${Config.values.databaseParameters}"
                    maximumPoolSize = 10
                    minimumIdle = 10
                    idleTimeout = 600000
                    maxLifetime = 1800000
                    keepaliveTime = 300000
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                    addDataSourceProperty("cachePrepStmts", "true")
                    addDataSourceProperty("prepStmtCacheSize", "250")
                    addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                    addDataSourceProperty("useServerPrepStmts", "true")
                    addDataSourceProperty("useUnicode", "true")
                    addDataSourceProperty("characterEncoding", "utf8mb4")
                    addDataSourceProperty("rewriteBatchedStatements", "true")
                    addDataSourceProperty("serverTimezone", "UTC")
                }
            }
        }

        dataSource = HikariDataSource(hikariCfg)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Players, Matches, MatchTeams, MatchPlayers,
                ShopPurchases, UpgradePurchases, MatchEvents, MatchEventMetas)
        }
    }
    fun close() {
        if (::dataSource.isInitialized) {
            (dataSource as? HikariDataSource)?.close()
        }
    }
}