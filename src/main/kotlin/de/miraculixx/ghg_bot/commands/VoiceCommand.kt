package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object VoiceCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val guild = it.guild ?: return
        val vc = it.getOption("channel")?.asChannel?.asVoiceChannel() ?: return

        guild.audioManager.openAudioConnection(vc)
        it.reply_("Joined ${vc.asMention}", ephemeral = true).queue()
    }
}
