package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import de.miraculixx.ghg_bot.utils.log.noGuild
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.time.toKotlinDuration

class WarnCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        if (it.guild == null) {
            it.reply_(noGuild).queue()
            return
        }

        val member = it.getOption("user")?.asMember
        if (member == null) {
            it.reply_("Der Nutzer wurde nicht gefunden!", ephemeral = true).queue()
            return
        }

        when (it.subcommandName) {
            "amount" -> {
                val amount = Warnings.getWarnings(member.id)
                it.reply_("Der Nutzer ${member.asMention} (${member.id}) hat aktuell **$amount** Warnings", ephemeral = true).queue()
            }
            "warn" -> {
                val reason = it.getOption("reason")?.asString ?: "Nicht angegeben"
                val duration = Warnings.warnMember(member, reason, true)
                it.reply_("Der Nutzer ${member.asMention} (${member.id}) wurde erfolgreich gewarnt für ``${duration.toKotlinDuration()}``!", ephemeral = true).queue()
            }
            "set-warns" -> {
                val amount = it.getOption("amount")?.asInt ?: 0
                Warnings.setWarns(member.id, amount)
                it.reply_("Die Warnings von ${member.asMention} (${member.id}) wurden erfolgreich auf **$amount** gesetzt!", ephemeral = true).queue()
            }
        }
    }

    override suspend fun triggerUserApp(it: UserContextInteractionEvent) {
        it.replyModal("WARN:${it.targetMember?.id}", "Warn User") {
            paragraph("reason", "Reason", true)
        }.queue()
    }
}