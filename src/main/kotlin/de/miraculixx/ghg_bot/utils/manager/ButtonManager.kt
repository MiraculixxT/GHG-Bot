package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.modules.notifications.NotifyButtons
import de.miraculixx.ghg_bot.modules.tickets.TicketButtonHandler
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ButtonManager {
    private val buttons = mapOf(
        "TICKET" to TicketButtonHandler(),
        "NOTIFY" to NotifyButtons()
    )

    fun startListen(jda: JDA) = jda.listener<ButtonInteractionEvent> {
        val id = it.button.id ?: return@listener
        val commandClass = when {
            id.startsWith("TICKET-") -> buttons["TICKET"]
            id.startsWith("NOTIFY") -> buttons["NOTIFY"]
            else -> buttons[id]
        }
        commandClass?.trigger(it)
    }
}