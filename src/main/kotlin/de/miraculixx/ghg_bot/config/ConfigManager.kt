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

    val fileSpecialChannels = File(configFolder, "special-channels.json")
    val specialChannels: SpecialChannels = fileSpecialChannels.loadConfig(SpecialChannels())


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

    @Serializable
    data class SpecialChannels(
        val guild: Long = 0,
        val threadChannel: MutableSet<Long> = mutableSetOf(),
        val fanart: FanartChannel = FanartChannel(),
        val whitelistedGuilds: MutableSet<Long> = mutableSetOf(),
        val modLog: Long = 0,
        val teamRole: Long = 0,
        val tickets: TicketChannels = TicketChannels(),
        val notifications: Notifications = Notifications(),
        val voiceCategory: Long = 0,
        val reportChannel: Long = 0
    )

    @Serializable
    data class FanartChannel(
        val channel: Long = 0,
        val awardRole: Long = 0,
        val highlightTag: Long = 0,
        val fanartTag: Long = 0
    )

    @Serializable
    data class Notifications(
        val ytMainRole: Long = 0,
        val ytSecondRole: Long = 0,
        val twitchRole: Long = 0
    )

    @Serializable
    data class TicketChannels(
        val channel: Long = 0,
        val archive: Long = 0,
        val reportRole: Long = 0,
        val questionRole: Long = 0
    )
}