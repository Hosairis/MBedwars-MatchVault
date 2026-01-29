package me.hosairis.matchvault.util

import me.hosairis.matchvault.MatchVault
import me.hosairis.matchvault.storage.config.Config

object Log {

    fun info(input: String) {
        MatchVault.instance.logger.info(input)
    }

    fun warning(input: String) {
        MatchVault.instance.logger.warning(input)
    }

    fun severe(input: String) {
        MatchVault.instance.logger.severe(input)
    }
}