package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.other.CommandOnlyChannel
import de.miraculixx.ghg_bot.modules.other.MediaOnlyChannel
import de.miraculixx.ghg_bot.modules.other.StickyMessage
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ChannelCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        it.guild ?: return
        val channelID = it.channel.idLong
        it.reply_(embeds = listOf(Embed {  }))

        when (it.subcommandName) {
            "command-only" -> {
                if (CommandOnlyChannel.channels.contains(channelID)) CommandOnlyChannel.channels.remove(it.notifyUser(false, "command-only"))
                else CommandOnlyChannel.channels.add(it.notifyUser(true, "command-only"))
            }

            "media-only" -> {
                if (MediaOnlyChannel.channels.contains(channelID)) MediaOnlyChannel.channels.remove(it.notifyUser(false, "media-only"))
                else MediaOnlyChannel.channels.add(it.notifyUser(true, "media-only"))
            }

            "sticky-message" -> {
                val option = it.getOption("message")?.asString ?: return
                if (option == ".") {
                    StickyMessage.stickyMessages.remove(channelID)
                    it.notifyUser(false, "sticky")
                } else {
                    StickyMessage.stickyMessages[channelID] = option
                    it.notifyUser(true, "sticky mit der Nachricht $option")
                }
            }
        }
    }

    private fun SlashCommandInteractionEvent.notifyUser(added: Boolean, name: String): Long {
        reply_("Der Channel ${channel.name} ist ${if (added) "jetzt" else "nicht mehr"} ``$name``", ephemeral = true).queue()
        return channel.idLong
    }
}