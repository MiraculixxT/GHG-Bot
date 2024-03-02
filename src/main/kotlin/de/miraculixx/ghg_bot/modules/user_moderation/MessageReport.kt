package de.miraculixx.ghg_bot.modules.user_moderation

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import de.miraculixx.ghg_bot.utils.extensions.mentionlessContent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds


data class MessageReport(
    val snitch: Member,
    val message: Message,
    var reason: String,
    val context: Set<Message>,
    val uuid: UUID = UUID.randomUUID(),
    val voteYes: MutableSet<UserTrust> = mutableSetOf(),
    val voteNo: MutableSet<UserTrust> = mutableSetOf()
) {
    private var finalMessage = ""
    private lateinit var reportCaseMessage: Message
    private lateinit var reportCaseThread: ThreadChannel

    suspend fun send() {
        finalMessage = message.mentionlessContent()

        reportCaseMessage = UserModerationManager.reportChannel.send(
            "<@&1193568003154006098>",
            embeds = listOf(buildEmbed(0 to 1)),
            components = listOf(
                ActionRow.of(
                    danger("REPORT:VOTE-YES:$uuid", "Schuldig"),
                    success("REPORT:VOTE-NO:$uuid", "Unschuldig")
                )
            )
        ).complete()

        // Create thread
        reportCaseThread = reportCaseMessage.createThreadChannel("Kontext zur Nachricht...").complete()
        val threadWebhook = UserModerationManager.webhookInnocent.onThread(reportCaseThread.idLong)

        // Send context messages
        val knownInnocents = mutableListOf<String>()
        context.forEach {
            val builder = WebhookMessageBuilder()
            // Check if the author is the reported user
            val currentID = it.author.id
            if (currentID == message.author.id) {
                builder.setUsername("Verdächtiger")
                builder.setAvatarUrl(UserModerationManager.iconGuilty)
            } else {
                val innoNumber = if (knownInnocents.contains(currentID)) knownInnocents.indexOf(currentID) + 1
                else {
                    knownInnocents.add(currentID)
                    knownInnocents.size
                }
                builder.setUsername("Innocent #$innoNumber")
                builder.setAvatarUrl(UserModerationManager.iconInnocent)
            }

            builder.setContent(it.mentionlessContent().ifBlank { "<Leere Nachricht>" })
            threadWebhook.send(builder.build())
        }

        // Send the reported message
        threadWebhook.send(
            WebhookMessageBuilder().apply {
                setContent(finalMessage)
                setUsername("Verdächtiger")
                setAvatarUrl(UserModerationManager.iconGuilty)
                message.attachments.forEach { attachment ->
                    addFile(attachment.fileName, attachment.proxy.download().await())
                }
            }.build()
        )
    }

    fun update(hook: InteractionHook, votes: Pair<Int, Int>) {
        hook.editOriginalEmbeds(buildEmbed(votes)).queue()
    }

    private fun buildEmbed(votes: Pair<Int, Int>) = Embed {
        println(votes.toString() + " - ${votes.first.toFloat() / (votes.second + votes.first)} - ${(votes.first.toFloat() / (votes.second + votes.first)) * 100}")
        val percentage = ((votes.first.toFloat() / (votes.second + votes.first)) * 100).roundToInt()
        title = "🔨 || Neue Meldung"
        field("Grund 📧", "```fix\n$reason ${if (message.attachments.isNotEmpty()) "(Anhänge siehe Kontext)" else ""}```", false)
        field("Nachricht \uD83D\uDCAC", "> ${finalMessage.ifBlank { "`<Leere Nachricht - Siehe Kontext>`" }}", false)
        field(
            "Aktuelle Entscheidung - $percentage% \uD83D\uDCCA",
            "<:zickzackApfeldieb:591330133349236774> `${voteYes.size}` <:blanc:1193179205589008455> <:blanc:1193179205589008455> <:peopHappy:592306163341721626> `${voteNo.size}`",
            false
        )
        color = 0xa31c1c
    }

    /**
     * @return Pair<Yes, No>
     */
    fun calculateVotes(): Pair<Int, Int> {
        var yes = 0.0f
        var no = 0.0f
        voteYes.forEach {
            yes += 1.0f * it.getLevelMultiplier()
        }
        voteNo.forEach {
            no += 1.0f * it.getLevelMultiplier()
        }
        return yes.roundToInt() to no.roundToInt()
    }

    suspend fun cleanUp(guilty: Boolean, votes: Pair<Int, Int>) {
        reportCaseThread.delete().queue()
        reportCaseMessage.editMessageComponents(emptyList()).queue()
        delay(1.seconds)
        reportCaseMessage.editMessageEmbeds(Embed {
            title = "🔨 || Meldung abgeschlossen"
            description = "Die Meldung wurde abgeschlossen und der Nutzer wurde${if (guilty) " " else " __**nicht**__ "}bestraft."
            color = 0xb5ac05
        }).queue()
        UserModerationManager.cases.remove(uuid)

        val reportLogData = ReportVoteData(guilty, snitch.idLong, message.author.idLong, voteYes.map { it.userID }.toSet(), voteNo.map { it.userID }.toSet())
        UserModerationManager.reportLogChannel.send("||${Json.encodeToString(reportLogData)}||", embeds = listOf(Embed {
            title = "🔨 || Meldung abgeschlossen"
            description = "Der Report wurde als __**${if (guilty) "schuldig" else "unschuldig"}**__ markiert. (${votes.first} | ${votes.second} -> ${votes.first / (votes.second + votes.first)}%)"
            field("Grund \uD83D\uDCE7", "```fix\n$reason```", false)
            field("Nachricht \uD83D\uDCAC", "```fix\n${message.mentionlessContent().ifBlank { "<empty>" }}```", false)
            field(
                "Finaler Vote \uD83D\uDCCA",
                "<:zickzackApfeldieb:591330133349236774> `${voteYes.size}` <:blanc:1193179205589008455> <:blanc:1193179205589008455> <:peopHappy:592306163341721626> `${voteNo.size}`",
                false
            )
            image = message.attachments.firstOrNull()?.url
        }), components = listOf(
            ActionRow.of(danger("REPORT-ADMIN:FALSE", "Falscher Vote"))
        )).queue()
    }
}

@Serializable
data class ReportVoteData(
    val guilty: Boolean,
    val snitch: Long,
    val victim: Long,
    val voteYes: Set<Long>,
    val voteNo: Set<Long>
)
