package de.miraculixx.ghg_bot.modules.user_moderation

import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.utils.entities.ModalEvent
import de.miraculixx.ghg_bot.utils.extensions.toMember
import de.miraculixx.ghg_bot.utils.extensions.toUUID
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

class ModalReport: ModalEvent {
    override suspend fun trigger(it: ModalInteractionEvent) {
        val data = it.modalId.split(":")
        val reportID = data.getOrNull(2)?.toUUID() ?: return
        val report = UserModerationManager.cases[reportID] ?: return

        when (data.getOrNull(1)) {
            "MESSAGE" -> { // Report creation - snitch entered a reason
                val reason = it.getValue("REASON")?.asString ?: return
                report.reason = reason
                it.reply_("## Danke für deine Meldung!\nSie wurde an das Team weitergeleitet.", ephemeral = true).queue()
                report.send()
            }

            "WARN" -> { // Moderator warned the reported user via the report action button
                val moderator = it.member ?: return
                val reason = it.getValue("REASON")?.asString ?: return
                val target = report.message.author.idLong.toMember()
                if (target == null) {
                    it.reply_("```diff\n- Der gemeldete Nutzer ist nicht mehr auf dem Server!```", ephemeral = true).queue()
                    return
                }
                Warnings.warnMember(target, reason, true)
                report.complete("Gewarnt", moderator)
                it.reply_("```diff\n+ Nutzer wurde gewarnt.```", ephemeral = true).queue()
            }
        }
    }
}
