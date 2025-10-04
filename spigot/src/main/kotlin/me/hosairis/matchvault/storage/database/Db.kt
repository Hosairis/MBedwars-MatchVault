package me.hosairis.matchvault.storage.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.File
import javax.sql.DataSource

class Db {
    companion object {
        lateinit var dataSource: DataSource
            private set

        fun init() {
            val dbType = Config.DATABASE_TYPE.lowercase().takeIf {
                it == "sqlite" || it == "mysql" || it == "mariadb"
            } ?: run {
                Bukkit.getLogger().severe("Unsupported database type: ${Config.DATABASE_TYPE}")
                Bukkit.getPluginManager().disablePlugin(MatchVault.getInst())
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

                        val dbFile = File(MatchVault.getAddon().dataFolder, "data.db")
                        dbFile.parentFile.mkdirs()
                        jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"

                        connectionInitSql = "PRAGMA foreign_keys=ON; PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL;"
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
        }

        fun close() {
            if (::dataSource.isInitialized) {
                (dataSource as? HikariDataSource)?.close()
            }
        }
    }
}