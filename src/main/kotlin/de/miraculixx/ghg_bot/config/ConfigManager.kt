package de.miraculixx.ghg_bot.config

import de.miraculixx.ghg_bot.modules.auto_support.SupportData
import de.miraculixx.ghg_bot.modules.auto_support.SupportFilter
import de.miraculixx.ghg_bot.utils.extensions.loadConfig
import de.miraculixx.ghg_bot.utils.extensions.saveConfig
import kotlinx.serialization.Serializable
import java.io.File

object ConfigManager {
    val configFolder = File("config")

    val fileCredentials = File(configFolder, "credentials.json")
    val credentials: Credentials = fileCredentials.loadConfig(Credentials())

    val fileModeration = File(configFolder, "moderation.json")
    val moderation: Moderation = fileModeration.loadConfig(Moderation())

    val fileAutoSupport = File(configFolder, "auto-support.json")
    val autoSupport: AutoSupport = fileAutoSupport.loadConfig(AutoSupport())

    fun save() {
        fileModeration.saveConfig(moderation)
        fileAutoSupport.saveConfig(autoSupport)
    }

    init {
        SupportData.buildRegex()
    }

    //
    // Config data classes
    //

    @Serializable
    data class Credentials(val dcToken: String = "<token>")

    @Serializable
    data class Moderation(
        val messageWebhook: String = "<link>",
        val supportPresets: MutableMap<String, String> = mutableMapOf()
    )

    @Serializable
    data class AutoSupport(
        val regexMap: MutableMap<SupportFilter, MutableList<String>> = mutableMapOf()
    )
}