package de.miraculixx.ghg_bot.modules.auto_support

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.cache.supportRegex
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class AutoSupportMessages: EventListener {
    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        val message = it.message
        val rawMessage = message.contentRaw
        if (message.author.isBot) return@listener

        supportRegex.forEach { (filter, regex) ->
            if (rawMessage.contains(regex)) {
                when (filter) {
                    SupportFilter.SPAM -> {
                        // Only fire if a picture is sent with
                        if (message.attachments.size > 0) message.replyEmbeds(filter.embed).queue()
                    }

                    else -> message.replyEmbeds(filter.embed).queue()
                }
            }
        }
    }
}