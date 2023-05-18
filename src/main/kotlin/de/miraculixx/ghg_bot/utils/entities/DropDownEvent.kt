package de.miraculixx.ghg_bot.utils.entities

import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

interface DropDownEvent {
    suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {}
}