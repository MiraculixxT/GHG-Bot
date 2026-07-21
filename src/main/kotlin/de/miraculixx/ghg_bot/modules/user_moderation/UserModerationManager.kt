package de.miraculixx.ghg_bot.modules.user_moderation

import club.minnced.discord.webhook.WebhookClientBuilder
import de.miraculixx.ghg_bot.JDA
import dev.minn.jda.ktx.generics.getChannel
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.io.File
import java.util.*

object UserModerationManager {
    val reportChannel = JDA.getChannel<MessageChannel>(1193169356239163432)!!
    private val credentialsFile = File("config/webhooks.json")

    val cases = mutableMapOf<UUID, MessageReport>()

    private val webhookCredentials = Json.decodeFromString<WebhookCredentials>(credentialsFile.readText())
    val contextWebhook = WebhookClientBuilder(webhookCredentials.innocent).build()

    private const val sapphirePrefix = "!"

    /** Build a ready-to-use Sapphire ban command for the given user. */
    fun sapphireBanCommand(userId: String, reason: String) =
        "${sapphirePrefix}ban $userId ${reason.replace("\n", " ").ifBlank { "Regelverstoß" }}"
}
