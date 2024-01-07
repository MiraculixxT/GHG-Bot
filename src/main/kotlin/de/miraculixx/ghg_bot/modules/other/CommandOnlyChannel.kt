package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.utils.cache.teamRole
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object CommandOnlyChannel : EventListener {
    val channels = listOf(1036242559959302164)

    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        val author = it.member ?: return@listener
        val id = it.channel.idLong
        if (channels.contains(id)) {
            if (author.user.isBot || author.roles.contains(teamRole)) return@listener
            it.message.delete().queue()
            Warnings.warnMember(author, "Bitte chatte nicht im Channel ${it.channel.name}! Hier sind nur Befehle erlaubt.", false, false)
        }
    }
}