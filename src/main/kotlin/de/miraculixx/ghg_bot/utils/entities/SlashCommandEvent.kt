package de.miraculixx.ghg_bot.utils.entities

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

interface SlashCommandEvent {
    suspend fun trigger(it: SlashCommandInteractionEvent)

    suspend fun triggerUserApp(it: UserContextInteractionEvent) {}

    suspend fun triggerMessageApp(it: MessageContextInteractionEvent) {}
}