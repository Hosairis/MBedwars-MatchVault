package me.hosairis.matchvault.storage.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import javax.sql.DataSource

object Db {
    lateinit var dataSource: DataSource
        private set

    fun init() {
        val dbType = Config.DATABASE_TYPE.lowercase().takeIf {
            it in listOf("h2", "sqlite", "mysql", "mariadb")
        } ?: run {
            Bukkit.getLogger().severe("Unsupported database type: ${Config.DATABASE_TYPE}")
            Bukkit.getPluginManager().disablePlugin(MatchVault.getInst())
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
                    val dbFile = File(MatchVault.getAddon().dataFolder, "data.sqlite.db")
                    dbFile.parentFile.mkdirs()
                    jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
                    connectionInitSql = "PRAGMA foreign_keys=ON; PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL;"
                }
                "h2" -> {
                    driverClassName = "org.h2.Driver"
                    maximumPoolSize = 4
                    minimumIdle = 1
                    val dbFile = File(MatchVault.getAddon().dataFolder, "data.h2")
                    dbFile.parentFile.mkdirs()
                    jdbcUrl = "jdbc:h2:file:${dbFile.absolutePath};MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE;DATABASE_TO_UPPER=FALSE;LOCK_TIMEOUT=10000;TRACE_LEVEL_FILE=0"
                }
                "mysql", "mariadb" -> {
                    driverClassName = "org.mariadb.jdbc.Driver"
                    username = Config.DATABASE_USER
                    password = Config.DATABASE_PASSWORD
                    jdbcUrl = "jdbc:mariadb://${Config.DATABASE_HOST}:${Config.DATABASE_PORT}/${Config.DATABASE_NAME}${Config.DATABASE_PARAMETERS}"
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
            SchemaUtils.create(
                Players, Matches, MatchTeams, MatchPlayers,
                ShopPurchases, UpgradePurchases, Timelines, TimelineMetas)

            val updateQueries = SchemaUtils.addMissingColumnsStatements(
                Players, Matches, MatchTeams, MatchPlayers,
                ShopPurchases, UpgradePurchases, Timelines, TimelineMetas)

            if (updateQueries.isNotEmpty()) {
                updateQueries.forEach { query -> exec(query) }
            }
        }
    }
    fun close() {
        if (::dataSource.isInitialized) {
            (dataSource as? HikariDataSource)?.close()
        }
    }
}