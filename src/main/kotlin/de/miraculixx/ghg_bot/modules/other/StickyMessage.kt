package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.entities.EventListener
import de.miraculixx.ghg_bot.utils.extensions.loadConfig
import de.miraculixx.ghg_bot.utils.extensions.saveConfig
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.File
import kotlin.time.Duration.Companion.seconds

object StickyMessage : EventListener {
    private val file = File("config", "sticky.json")
    val stickyMessages: MutableMap<Long, String> = file.loadConfig(mutableMapOf())
    private val lastSticky: MutableMap<Long, Message> = mutableMapOf()
    private val cooldown: MutableSet<Long> = mutableSetOf()

    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        if (it.author.isBot) return@listener
        val channel = it.channel.idLong

        // Cooldown
        if (cooldown.contains(channel)) return@listener
        cooldown.add(channel)
        CoroutineScope(Dispatchers.Default).launch {
            delay(5.seconds)
            cooldown.remove(channel)
        }

        // Message
        val content = stickyMessages[channel] ?: return@listener
        lastSticky[channel]?.delete()?.queue()
        lastSticky[channel] = it.channel.sendMessage(content).complete()
    }

    fun save() {
        file.saveConfig(stickyMessages)
    }
}