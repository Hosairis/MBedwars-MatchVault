package me.hosairis.matchvault.storage.config

import dev.dejvokep.boostedyaml.YamlDocument
import me.hosairis.matchvault.util.MessageHelper

object Messages : AbstractConfig("messages.yml") {

    data class Values(
        val prefix: String,

        val reloadSuccess: String,
        val reloadFailed: String,
        val insufficientPermissions: String,
        val consoleProhibitedCommand: String,

        val configVersion: Int,
    )

    @Volatile
    var values: Values = Values(
        prefix = "&7[&bMatchVault&7]&r",

        reloadSuccess = "%prefix &a✔ &7Reload &aSuccessful",
        reloadFailed = "%prefix &c✖ &7Reload &cFailed",
        insufficientPermissions = "%prefix &c✖ &7You lack the required permission: &c%permission",
        consoleProhibitedCommand = "%prefix &c✖ &7Console is not allowed to execute this command",

        configVersion =  1,
    )
        private set

    override fun loadValues(doc: YamlDocument) {
        values = Values(
            prefix = doc.getString("prefix"),

            reloadSuccess = setPlaceHolders(doc.getString("reload-success")),
            reloadFailed = setPlaceHolders(doc.getString("reload-failed")),
            insufficientPermissions = setPlaceHolders(doc.getString("insufficient-permissions")),
            consoleProhibitedCommand = setPlaceHolders(doc.getString("console-prohibited-command")),

            configVersion = doc.getInt("config-version"),
        )
    }

    private fun  setPlaceHolders(input: String): String {
        return MessageHelper.colorize(input).replace("%prefix", values.prefix)
    }
}