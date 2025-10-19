package me.hosairis.matchvault.helpers

import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config

object Log {
    fun info(input: String) {
        MatchVault.getInst().logger.info(input)
    }

    fun warning(input: String) {
        MatchVault.getInst().logger.warning(input)
    }

    fun severe(input: String) {
        MatchVault.getInst().logger.severe(input)
    }

    fun debug(input: String) {
        if (!Config.ADVANCED_DEBUG) return
        MatchVault.getInst().logger.info("[DEBUG] $input")
    }
}