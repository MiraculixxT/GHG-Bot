package de.miraculixx.ghg_bot.utils.entities

import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent

interface DropDownEvent {
    suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {}
}