package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MessageCommand : SlashCommandEvent {

    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        it.guild ?: return

        when (it.subcommandName) {
            "say" -> {
                val message = it.getOption("message")?.asString ?: return
                it.channel.send(message).queue()
                it.reply_("Message sent!", ephemeral = true).queue()
            }

            "components" -> {
                it.reply_("Moved to Message Apps -> Edit Components", ephemeral = true).queue()
            }
        }
    }

    override suspend fun triggerMessageApp(it: MessageContextInteractionEvent) {
        val message = it.target
        val components = message.components
    }
}