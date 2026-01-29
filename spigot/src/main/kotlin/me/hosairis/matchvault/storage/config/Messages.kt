package me.hosairis.matchvault.storage.config

import dev.dejvokep.boostedyaml.YamlDocument

object Messages : AbstractConfig("messages.yml") {

    data class Values(
        val configVersion: Int
    )

    @Volatile
    var values: Values = Values(
        configVersion =  1
    )
        private set

    override fun loadValues(doc: YamlDocument) {
        values = Values(
            configVersion = doc.getInt("config-version")
        )
    }
}