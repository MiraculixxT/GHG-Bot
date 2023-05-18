package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class LetMeGoogleCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val prompt = it.getOption("prompt")?.asString ?: return
        val ping = it.getOption("ping")?.asMember

        //https://letmegooglethat.com/?q=minecraft+random+item+every+30+seconds
        it.reply_("${if (ping != null) "${ping.asMention}\n" else ""} <https://letmegooglethat.com/?q=${prompt.replace(' ', '+')}>").queue()
    }
}