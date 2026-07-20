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

        it.reply_("## Danke für deine Meldung!\nUnser Team wird sich die Meldung angucken und möglichst bald bearbeiten.", ephemeral = true).queue()
        report.send()
    }
}