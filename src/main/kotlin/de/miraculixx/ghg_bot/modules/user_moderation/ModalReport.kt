package de.miraculixx.ghg_bot.modules.user_moderation

import de.miraculixx.ghg_bot.utils.entities.ModalEvent
import de.miraculixx.ghg_bot.utils.extensions.toUUID
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

class ModalReport: ModalEvent {
    override suspend fun trigger(it: ModalInteractionEvent) {
        val data = it.modalId.split(":")
        val reportID = data.getOrNull(2)?.toUUID() ?: return

        val reason = it.getValue("REASON")?.asString ?: return
        val report = UserModerationManager.cases[reportID] ?: return
        report.reason = reason

        it.reply_("## Danke für deine Meldung!\nDu kannst den Status deiner aktuellen Meldung in <#1193169356239163432> (<#1193168949735596093>) nachschauen.", ephemeral = true).queue()
        report.send()
    }
}