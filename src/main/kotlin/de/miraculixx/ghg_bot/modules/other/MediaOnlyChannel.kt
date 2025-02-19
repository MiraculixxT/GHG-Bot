package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.utils.cache.teamRole
import de.miraculixx.ghg_bot.utils.entities.EventListener
import de.miraculixx.ghg_bot.utils.extensions.loadConfig
import de.miraculixx.ghg_bot.utils.extensions.saveConfig
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.File

object MediaOnlyChannel : EventListener {
    private val file = File("config", "media-only.json")
    val channels = file.loadConfig<MutableSet<Long>>(mutableSetOf())

    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        val author = it.member ?: return@listener
        val id = it.channel.idLong
        if (channels.contains(id)) {
            if (it.message.attachments.isNotEmpty()) return@listener
            if (author.user.isBot || author.roles.contains(teamRole)) return@listener
            it.message.delete().queue()
            Warnings.warnMember(author, "Bitte chatte nicht im Channel ${it.channel.name}!\nHier sind nur Bilder erlaubt, erstelle einen Thread um über ein Bild zu schreiben", false, false)
        }
    }

    fun save() {
        file.saveConfig(channels)
    }
}