package de.miraculixx.ghg_bot.modules.user_moderation

import club.minnced.discord.webhook.WebhookClientBuilder
import de.miraculixx.ghg_bot.JDA
import dev.minn.jda.ktx.generics.getChannel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.io.File
import java.util.*

object UserModerationManager {
    val reportChannel = JDA.getChannel<MessageChannel>(1193169356239163432)!!
    val reportLogChannel = JDA.getChannel<MessageChannel>(1193556954468077799)!!
    val voteRole = JDA.getRoleById(1193568003154006098)!!
    private val saveFile = File("config/user_moderation.json")
    private val credentialsFile = File("config/webhooks.json")

    val cases = mutableMapOf<UUID, MessageReport>()
    val userTrust = mutableMapOf<Long, UserTrust>()

    private val webhookCredentials = Json.decodeFromString<WebhookCredentials>(credentialsFile.readText())
    val webhookInnocent = WebhookClientBuilder(webhookCredentials.innocent).build()
    const val iconGuilty = "https://cdn.discordapp.com/emojis/591330133349236774.webp?quality=lossless"
    const val iconInnocent = "https://cdn.discordapp.com/emojis/1059233059532197979.webp?quality=lossless"
    val levelPointsBase = 100

    fun save() {
        saveFile.writeText(Json.encodeToString(userTrust))
    }


    init {
        if (saveFile.exists()) {
            userTrust.putAll(Json.decodeFromString(saveFile.readText()))
        }
    }
}