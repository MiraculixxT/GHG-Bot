package de.miraculixx.ghg_bot.utils.serializer

import de.miraculixx.ghg_bot.JDA
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel

class VoiceChannelSerializer: KSerializer<VoiceChannel> {
    override val descriptor = PrimitiveSerialDescriptor("VoiceChannel", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): VoiceChannel {
        return JDA.getVoiceChannelById(decoder.decodeLong())!!
    }

    override fun serialize(encoder: Encoder, value: VoiceChannel) {
        encoder.encodeLong(value.idLong)
    }
}