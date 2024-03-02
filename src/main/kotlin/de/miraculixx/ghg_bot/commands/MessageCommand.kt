package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.time.format.DateTimeFormatter

class MessageCommand : SlashCommandEvent {
    private val formatter = DateTimeFormatter.ISO_INSTANT
    private val parser = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        it.guild ?: return

        when (it.subcommandName) {
            "say-json" -> {
                val json = it.getOption("json")?.asString ?: return
                val upload = it.getOption("upload")?.asAttachment

                val message = try {
                    parser.decodeFromString<RawMessage>(json)
                } catch (e: Exception) {
                    it.reply_("Invalid JSON!\n${e.message}", ephemeral = true).queue()
                    return
                }

                it.reply_("Your message is processing...", ephemeral = true).queue()

                it.channel.send(
                    message.content ?: "",
                    message.embeds?.map { embed ->
                        Embed {
                            title = embed.title
                            description = embed.description
                            url = embed.url
                            color = embed.color
                            embed.fields?.forEach { field ->
                                field(field.name, field.value, field.inline)
                            }
                            embed.author?.let { author -> author(author.name, author.url, author.icon_url) }
                            embed.footer?.let { footer -> footer(footer.text, footer.icon_url) }
                            image = embed.image?.url
                            thumbnail = embed.thumbnail?.url
                            timestamp = formatter.parse(embed.timestamp)
                        }
                    } ?: emptySet(),
                    files = buildList {
                        if (upload != null) {
                            try {
                                add(FileUpload.fromData(upload.proxy.download().get(), upload.fileName))
                            } catch (_: Exception) {}
                        }
                    }
                ).queue()
            }

            "say" -> {
                val message = it.getOption("message")?.asString ?: return
                it.channel.send(message).queue()
                it.reply_("Message sent!", ephemeral = true).queue()
            }
        }
    }

    @Serializable
    private data class RawMessage(
        val content: String?,
        val embeds: List<RawEmbed>?
    )

    @Serializable
    private data class RawEmbed(
        val title: String? = null,
        val description: String? = null,
        val url: String? = null,
        val color: Int? = null,
        val fields: List<RawEmbedField>? = null,
        val author: RawEmbedAuthor? = null,
        val footer: RawEmbedFooter? = null,
        val image: RawEmbedImage? = null,
        val thumbnail: RawEmbedImage? = null,
        val timestamp: String? = null
    )

    @Serializable
    private data class RawEmbedField(
        val name: String,
        val value: String,
        val inline: Boolean = false
    )

    @Serializable
    private data class RawEmbedAuthor(
        val name: String,
        val url: String? = null,
        val icon_url: String? = null
    )

    @Serializable
    private data class RawEmbedFooter(
        val text: String,
        val icon_url: String? = null
    )

    @Serializable
    private data class RawEmbedImage(
        val url: String
    )
}