package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.user_moderation.MessageReport
import de.miraculixx.ghg_bot.modules.user_moderation.UserModerationManager
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

class ReportCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun triggerMessageApp(it: MessageContextInteractionEvent) {
        val member = it.member ?: return
        val message = it.interaction.target

        val history = it.messageChannel.getHistoryBefore(message, 10).complete().retrievedHistory.asReversed()
        val report = MessageReport(member, message, "", history.toSet())
        UserModerationManager.cases[report.uuid] = report
        val offset = Duration.between(message.timeCreated, OffsetDateTime.now());
        if (offset.toDays() >= 7) {
            it.reply_("```diff\n- Die Nachricht ist zu alt um gemeldet zu werden!```", ephemeral = true).queue()
            return
        }

        it.replyModal("REPORT:MESSAGE:${report.uuid}", "Nachricht Melden") {
            paragraph("REASON", "Grund", true, placeholder = "Warum möchtest du die Nachricht melden?") {
                minLength = 15
            }
        }.queue()
    }

    override suspend fun triggerUserApp(it: UserContextInteractionEvent) {
        val target = it.interaction.targetMember ?: return
        it.replyModal("TICKET-REPORT", "Nutzer Melden") {
            short("TAG", "Nutzer Tag", true, target.user.asTag) {
                minLength = 6
            }
            short("ID", "Nutzer ID", true, target.id, null, 17..20)
            paragraph("CONTENT", "Grund", true, null, "Warum möchtest du den Nutzer melden?") {
                minLength = 50
            }
        }.queue()
    }


}