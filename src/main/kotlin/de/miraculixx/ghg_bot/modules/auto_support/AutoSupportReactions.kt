package de.miraculixx.ghg_bot.modules.auto_support

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

class AutoSupportReactions: EventListener {
    override val listener: CoroutineEventListener = JDA.listener<MessageReactionAddEvent> {
        try {
            val message = it.retrieveMessage().complete()
            val owner = message.author
            if (owner.idLong != it.jda.selfUser.idLong) return@listener

            val repliedMessage = message.referencedMessage ?: return@listener
            if (repliedMessage.author.idLong != it.userIdLong || it.reaction.emoji.asReactionCode != "\uD83D\uDDD1️") {
                it.reaction.clearReactions().queue()
                return@listener
            }
            message.delete().queue()
        } catch (e: NullPointerException) {
            // Idk warum oder wann die kommt
            return@listener
        }
    }
}