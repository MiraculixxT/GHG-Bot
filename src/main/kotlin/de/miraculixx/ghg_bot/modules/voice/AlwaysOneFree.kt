package de.miraculixx.ghg_bot.modules.voice

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.entities.EventListener
import de.miraculixx.ghg_bot.utils.serializer.VoiceChannelSerializer
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import java.io.File

object AlwaysOneFree : EventListener {
    private val file = File("config/voice.json")

    private val category = JDA.getCategoryById(486142012194684928)!!
    private val channelsUnlimited: MutableSet<VoiceChannel> = mutableSetOf(JDA.getVoiceChannelById(1185571848721801256)!!)
    private val channels2: MutableSet<VoiceChannel> = mutableSetOf(JDA.getVoiceChannelById(1185596058714243152)!!)
    private val channels3: MutableSet<VoiceChannel> = mutableSetOf(JDA.getVoiceChannelById(1185596259290075136)!!)
    private val channels4: MutableSet<VoiceChannel> = mutableSetOf(JDA.getVoiceChannelById(1185596226926805042)!!)
    private val channels5: MutableSet<VoiceChannel> = mutableSetOf(JDA.getVoiceChannelById(1185596289493250098)!!)

    override val listener: CoroutineEventListener = JDA.listener<GuildVoiceUpdateEvent> {
        val joined = it.channelJoined?.asVoiceChannel()
        val left = it.channelLeft?.asVoiceChannel()

        println("$joined - $left")

        if (joined != null) {
            when (val limit = joined.userLimit) {
                0 -> joined.checkJoined(channelsUnlimited, limit)
                2 -> joined.checkJoined(channels2, limit)
                3 -> joined.checkJoined(channels3, limit)
                4 -> joined.checkJoined(channels4, limit)
                5 -> joined.checkJoined(channels5, limit)
            }
        }

        if (left != null) {
            when (left.userLimit) {
                0 -> left.checkLeft(channelsUnlimited)
                2 -> left.checkLeft(channels2)
                3 -> left.checkLeft(channels3)
                4 -> left.checkLeft(channels4)
                5 -> left.checkLeft(channels5)
            }
        }
    }

    private fun VoiceChannel.checkJoined(allChannels: MutableSet<VoiceChannel>, limit: Int) {
        var foundEmpty = false
        allChannels.forEach { voice ->
            if (voice.members.isEmpty()) foundEmpty = true
        }

        if (allChannels.size >= 6) {
            // Already enough channels
            return
        }

        // Create new if all not empty
        if (!foundEmpty) {
            val suffix = if (limit == 0) "∞" else limit.toString()
            val newChannel = category.createVoiceChannel("Talk → $suffix")
                .setUserlimit(limit).complete()
            category.modifyVoiceChannelPositions().sortOrder { o1, o2 ->
                (o1 as VoiceChannel).userLimit - (o2 as VoiceChannel).userLimit
            }.complete()
            allChannels.add(newChannel)
        }
    }

    private fun VoiceChannel.checkLeft(allChannels: MutableSet<VoiceChannel>) {
        if (members.isNotEmpty()) return // Only remove if empty
        if (this == allChannels.first()) { // Do not remove core channel
            val last = allChannels.last()
            if (last == this) return // If last == first only one exists
            if (last.members.isEmpty()) { // Delete last channel empty instead of core
                allChannels.remove(last)
                last.delete().complete()
            }

        } else { // Delete current empty channel
            allChannels.remove(this)
            delete().complete()
        }
    }

    fun save() {
        val content = Json.encodeToString(TempChannels(channelsUnlimited, channels2, channels3, channels4, channels5))
        file.writeText(content)
    }

    init {
        if (file.exists()) {
            val content = file.readText()
            try {
                val data = Json.decodeFromString<TempChannels>(content)
                channelsUnlimited.addAll(data.channelsUnlimited)
                channels2.addAll(data.channels2)
                channels3.addAll(data.channels3)
                channels4.addAll(data.channels4)
                channels5.addAll(data.channels5)
            } catch (_: Exception) {
            }
        }
    }

    @Serializable
    private data class TempChannels(
        val channelsUnlimited: Set<@Serializable(with = VoiceChannelSerializer::class) VoiceChannel>,
        val channels2: Set<@Serializable(with = VoiceChannelSerializer::class) VoiceChannel>,
        val channels3: Set<@Serializable(with = VoiceChannelSerializer::class) VoiceChannel>,
        val channels4: Set<@Serializable(with = VoiceChannelSerializer::class) VoiceChannel>,
        val channels5: Set<@Serializable(with = VoiceChannelSerializer::class) VoiceChannel>,
    )
}