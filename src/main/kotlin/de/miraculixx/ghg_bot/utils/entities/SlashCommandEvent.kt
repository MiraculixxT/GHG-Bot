package de.miraculixx.ghg_bot.utils.entities

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface SlashCommandEvent {
    suspend fun trigger(it: SlashCommandInteractionEvent)
}