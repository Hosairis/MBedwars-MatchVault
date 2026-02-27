package me.hosairis.matchvault.update

import com.google.gson.Gson
import me.hosairis.matchvault.util.Log
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    @Volatile
    var isOutdated: Boolean = false
        private set

    private var task: BukkitTask? = null

    fun start(
        plugin: JavaPlugin,
        currentVersion: String,
        apiUrl: String = "https://dreamers.dev/apps/versions.php?name=MB-MatchVault",
        periodTicks: Long = 12000L,
        initialDelayTicks: Long = 0L,
    ) {
        stop()

        val runnable = object : BukkitRunnable() {
            override fun run() {
                try {
                    val connection = openConnection(apiUrl)
                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        return
                    }

                    val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val jsonData = try {
                        Gson().fromJson(response, ApiData::class.java)
                    } catch (_: Exception) {
                        null
                    }

                    val latest = jsonData?.data?.version ?: return
                    if (!jsonData.ok) {
                        return
                    }

                    if (latest != currentVersion) {
                        isOutdated = true
                        cancel()
                        task = null
                    }
                } catch (e: Exception) {
                    Log.severe("Update check failed: ${e.message}")
                }
            }
        }

        task = if (periodTicks <= 0L) {
            runnable.runTaskAsynchronously(plugin)
        } else {
            runnable.runTaskTimerAsynchronously(plugin, initialDelayTicks, periodTicks)
        }
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun openConnection(url: String): HttpURLConnection {
        var currentUrl = URL(url)
        var connection = currentUrl.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (UpdateChecker; Spigot/Paper)"
        )

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
            responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
            responseCode == HttpURLConnection.HTTP_SEE_OTHER
        ) {
            val location = connection.getHeaderField("Location")
            if (!location.isNullOrBlank()) {
                currentUrl = URL(location)
                connection = currentUrl.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (UpdateChecker; Spigot/Paper)"
                )
            }
        }

        return connection
    }

    private data class ApiData(val ok: Boolean, val data: Data, val message: String)
    private data class Data(val name: String, val version: String)
}