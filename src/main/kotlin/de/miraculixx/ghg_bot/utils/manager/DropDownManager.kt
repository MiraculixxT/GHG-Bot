package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.modules.tickets.TicketDropDownHandler
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

object DropDownManager {
    private val dropdowns = mapOf(
        "TICKET" to TicketDropDownHandler()
    )

    fun startListen(jda: JDA) = jda.listener<GenericSelectMenuInteractionEvent<String, StringSelectMenu>> {
        val id = it.componentId
        val commandClass = when {
            else -> dropdowns[id]
        }
        commandClass?.trigger(it)
    }
}