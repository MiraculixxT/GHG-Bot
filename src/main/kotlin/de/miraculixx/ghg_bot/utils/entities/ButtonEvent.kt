package de.miraculixx.ghg_bot.utils.entities

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface ButtonEvent {
    suspend fun trigger(it: ButtonInteractionEvent) {}
}