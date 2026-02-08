package me.hosairis.matchvault.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TimeUtil {

    fun formatMillis(
        epochMillis: Long
    ): String {
        val zonedDateTime: ZonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a")

        return zonedDateTime.format(formatter)
    }

    fun formatDuration(
        millis: Long,
        includeMillis: Boolean = false,
        shortFormat: Boolean = true
    ): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        val remMinutes = minutes % 60
        val remSeconds = seconds % 60
        val remMillis = millis % 1000

        val parts = mutableListOf<String>()

        if (hours > 0) {
            parts += if (shortFormat) "$hours"
            else "$hours hour${if (hours > 1) "s" else ""}"
        }

        if (remMinutes > 0) {
            parts += if (shortFormat) "$remMinutes" + "m"
            else "$remMinutes minute${if (remMinutes > 1) "s" else ""}"
        }

        if (remSeconds > 0) {
            parts += if (shortFormat) "$remSeconds" + "s"
            else "$remSeconds second${if (remSeconds > 1) "s" else ""}"
        }

        if (includeMillis && remMillis > 0) {
            parts += "${remMillis}ms"
        }

        return when {
            parts.isNotEmpty() -> parts.joinToString(" ")
            includeMillis && remMillis > 0 -> "${remMillis}ms"
            else -> if (shortFormat) "0s" else "0 seconds"
        }
    }

}
