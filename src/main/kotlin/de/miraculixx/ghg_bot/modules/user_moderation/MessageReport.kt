package de.miraculixx.ghg_bot.modules.user_moderation

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import de.miraculixx.ghg_bot.utils.extensions.mentionlessContent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.interactions.components.secondary
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.util.*
import kotlinx.coroutines.future.await as awaitFuture


data class MessageReport(
    val snitch: Member,
    val message: Message,
    var reason: String,
    val context: Set<Message>,
    val uuid: UUID = UUID.randomUUID()
) {
    private var finalMessage = ""
    private lateinit var reportCaseMessage: Message
    private lateinit var reportCaseThread: ThreadChannel

    suspend fun send() {
        finalMessage = message.mentionlessContent()

        reportCaseMessage = UserModerationManager.reportChannel.send(
            embeds = listOf(buildEmbed()),
            components = listOf(
                ActionRow.of(
                    secondary("REPORT:IGNORE:$uuid", "Ignorieren"),
                    primary("REPORT:WARN:$uuid", "Warnen"),
                    danger("REPORT:BAN:$uuid", "Bannen")
                )
            )
        ).await()

        // Create context thread (non-anonymized - shows real authors)
        reportCaseThread = reportCaseMessage.createThreadChannel("Kontext zur Nachricht...").await()
        val threadWebhook = UserModerationManager.contextWebhook.onThread(reportCaseThread.idLong)

        // Send context messages with each author's real name and avatar
        context.forEach {
            threadWebhook.send(
                WebhookMessageBuilder().apply {
                    setContent(it.mentionlessContent().ifBlank { "<Leere Nachricht>" })
                    setUsername(it.author.effectiveName)
                    setAvatarUrl(it.author.effectiveAvatarUrl)
                }.build()
            )
        }

        // Send the reported message
        threadWebhook.send(
            WebhookMessageBuilder().apply {
                setContent(finalMessage.ifBlank { "<Leere Nachricht>" })
                setUsername(message.author.effectiveName)
                setAvatarUrl(message.author.effectiveAvatarUrl)
                message.attachments.forEach { attachment ->
                    addFile(attachment.fileName, attachment.proxy.download().awaitFuture())
                }
            }.build()
        )
    }

    private fun buildEmbed() = Embed {
        title = "🔨 || Neue Meldung"
        field("User", "${message.author.asMention} (`${message.author.id}`)", false)
        field("Reporter", "${snitch.asMention} (`${snitch.id}`)", false)
        field("Grund", "```fix\n$reason ${if (message.attachments.isNotEmpty()) "(Anhänge siehe Kontext)" else ""}```", false)
        field("Nachricht", "```${finalMessage.ifBlank { "<Leere Nachricht - Siehe Kontext>" }}```", false)
        color = 0xa31c1c
    }

    /**
     * Mark the report as handled: remove the action buttons and update the embed.
     * The context thread is kept as a record.
     */
    fun complete(action: String, moderator: Member) {
        reportCaseMessage.editMessageComponents(emptyList()).queue()
        reportCaseMessage.editMessageEmbeds(Embed {
            title = "🔨 || Meldung abgeschlossen"
            description = "Die Meldung wurde von ${moderator.asMention} als **$action** abgeschlossen."
            field("User", "${message.author.asMention} (`${message.author.id}`)", false)
            field("Grund", "```fix\n$reason```", false)
            color = 0xb5ac05
        }).queue()
        UserModerationManager.cases.remove(uuid)
    }
}
