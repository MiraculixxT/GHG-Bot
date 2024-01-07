package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.modules.tickets.TicketModalHandler
import de.miraculixx.ghg_bot.modules.user_moderation.ModalReport
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

object ModalManager {
    private val modals = mapOf(
        "TICKET" to TicketModalHandler,
        "WARN" to Warnings,
        "REPORT" to ModalReport()
    )

    fun startListen(jda: JDA) = jda.listener<ModalInteractionEvent> {
        val id = it.modalId
        val commandClass = when {
            id.startsWith("TICKET") -> modals["TICKET"]
            id.startsWith("WARN") -> modals["WARN"]
            id.startsWith("REPORT") -> modals["REPORT"]
            else -> modals[id]
        }
        commandClass?.trigger(it)
    }
}