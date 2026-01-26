package me.hosairis.matchvault.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TimeUtil {
    fun formatMillis(
        epochMillis: Long
    ): String {
        // 1️⃣ Convert the raw millis to a ZonedDateTime in the desired zone
        val zonedDateTime: ZonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())

        // 2️⃣ Define the output pattern.
        //    - yyyy → 4‑digit year
        //    - MM   → 2‑digit month (01‑12)
        //    - dd   → 2‑digit day of month
        //    - hh   → hour‑of‑am‑pm (01‑12)
        //    - mm   → minute (00‑59)
        //    - ss   → second (00‑59)
        //    - a    → AM/PM marker
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a")

        // 3️⃣ Apply the formatter
        return zonedDateTime.format(formatter)
    }

    fun formatDuration(millis: Long, includeMillis: Boolean = false, shortFormat: Boolean = true): String {
        val seconds = (millis / 1000)
        val minutes = seconds / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        val remainingSeconds = seconds % 60
        val remainingMillis = millis % 1000

        val parts = mutableListOf<String>()

        if (hours > 0) {
            parts.add("${hours}${if (shortFormat) "h" else " hour"}${if(hours > 1) "s" else ""}")
        }
        if (remainingMinutes > 0) {
            parts.add("${remainingMinutes}${if (shortFormat) "m" else " minute"}${if(remainingMinutes > 1) "s" else ""}")
        }
        if (remainingSeconds > 0) {
            parts.add("${remainingSeconds}${if (shortFormat) "s" else " second"}${if(remainingSeconds > 1 && !shortFormat) "s" else ""}")
        }
        if (includeMillis && remainingMillis > 0) {
            parts.add("${remainingMillis}ms")
        }

        return if (parts.isEmpty()) {
            "0s"
        } else {
            parts.joinToString(" ")
        }
    }
}
