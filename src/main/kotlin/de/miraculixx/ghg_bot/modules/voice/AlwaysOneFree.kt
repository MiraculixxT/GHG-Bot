package de.miraculixx.ghg_bot.modules.voice

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent

object AlwaysOneFree : EventListener {
    private val category = JDA.getCategoryById(486142012194684928)!!

    override val listener: CoroutineEventListener = JDA.listener<GuildVoiceUpdateEvent> {
        val joined = it.channelJoined?.asVoiceChannel()
        val left = it.channelLeft?.asVoiceChannel()

        println("$joined - $left")
        joined?.checkJoined()
        left?.checkLeft()
    }

    private fun VoiceChannel.checkJoined() {
        val type = VoiceChannelType.byIdentifier(name.last()) ?: return
        val foundEmpty = type.currentChannels.any { it.members.isEmpty() }

        if (type.currentChannels.size >= 6) {
            // Already enough channels
            return
        }

        // Create new if all not empty
        if (!foundEmpty) {
            val limit = type.identifier.digitToIntOrNull() ?: 0
            val newChannel = category.createVoiceChannel("Talk → ${type.identifier}")
                .setUserlimit(limit).complete()
            type.currentChannels.add(newChannel)
            sortChannels()
        }
    }

    private fun VoiceChannel.checkLeft() {
        val type = VoiceChannelType.byIdentifier(name.last()) ?: return
        val allChannels = type.currentChannels

        // Only remove if empty
        if (members.isNotEmpty()) return

        // Always leave one channel
        if (allChannels.size > 1) {
            allChannels.remove(this)
            delete().complete()
            sortChannels()
        }
    }

    private fun sortChannels() {
        category.modifyVoiceChannelPositions().sortOrder { o1, o2 ->
            (o1 as VoiceChannel).userLimit - (o2 as VoiceChannel).userLimit
        }.queue()
    }

    init {
        // Load channels
        category.channels.forEach {
            val identifier = it.name.last()
            val type = VoiceChannelType.byIdentifier(identifier) ?: return@forEach
            type.currentChannels.add(it as VoiceChannel)
        }

        // Create missing channels
        VoiceChannelType.entries.forEach { type ->
            if (type.currentChannels.isEmpty()) {
                val new = category.createVoiceChannel("Talk → ${type.identifier}")
                    .setUserlimit(type.identifier.digitToIntOrNull() ?: 0)
                    .complete()
                type.currentChannels.add(new)
            }
        }
        sortChannels()
    }
}