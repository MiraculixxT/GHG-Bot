package de.miraculixx.ghg_bot.modules.voice

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel


enum class VoiceChannelType(val identifier: Char, val currentChannels: MutableList<VoiceChannel> = mutableListOf()) {
    UNLIMITED('∞'),
    TWO('2'),
    THREE('3'),
    FOUR('4'),
    FIVE('5');

    companion object {
        fun byIdentifier(identifier: Char): VoiceChannelType? {
            return entries.find { it.identifier == identifier }
        }
    }
}