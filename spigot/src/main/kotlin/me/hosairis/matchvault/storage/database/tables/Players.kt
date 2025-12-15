package me.hosairis.matchvault.storage.database.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object Players : LongIdTable("players") {
    val name = varchar("name", 64).index()
    val uuid = varchar("uuid", 36).uniqueIndex()
    val firstSeen = long("first_seen")
    val lastSeen = long("last_seen")
}