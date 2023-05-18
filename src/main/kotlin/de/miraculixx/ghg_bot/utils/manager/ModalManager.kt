package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.modules.tickets.TicketModalHandler
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

object ModalManager {
    private val modals = mapOf(
        "TICKET" to TicketModalHandler()
    )

    fun startListen(jda: JDA) = jda.listener<ModalInteractionEvent> {
        val id = it.modalId
        val commandClass = when {
            id.startsWith("TICKET") -> modals["TICKET"]
            else -> modals[id]
        }
        commandClass?.trigger(it)
    }
}