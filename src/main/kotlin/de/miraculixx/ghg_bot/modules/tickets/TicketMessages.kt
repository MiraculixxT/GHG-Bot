package de.miraculixx.ghg_bot.modules.tickets

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.cache.ticketChannelID
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class TicketMessages : EventListener {
    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        val reference = it.message.referencedMessage ?: return@listener

        val thread = it.channel
        if (thread !is ThreadChannel) return@listener
        val parentChannel = thread.parentChannel
        if (parentChannel.idLong != ticketChannelID) return@listener
        if (reference.author.idLong != JDA.selfUser.idLong) return@listener

        thread.getHistoryFromBeginning(1).queue { history ->
            CoroutineScope(Dispatchers.Default).launch {
                val firstMessage = history.retrievedHistory.firstOrNull()?.contentRaw
                val ownerID = firstMessage?.split('-')?.getOrNull(1) ?: return@launch
                val owner = JDA.getUserById(ownerID) ?: JDA.retrieveUserById(ownerID).complete()
                it.message.reply_(owner.asMention).mentionRepliedUser(false).queue()
            }
        }
    }
}