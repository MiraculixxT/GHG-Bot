package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.message_editor.data.RawMessage
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
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

                it.reply_("Your message is processing...", ephemeral = true).queue()

                val message = try {
                    parser.decodeFromString<RawMessage>(json)
                } catch (e: Exception) {
                    it.hook.editMessage(content = "Invalid JSON!\n```fix\n${e.message}```").queue()
                    return
                }

                try {
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
                                timestamp = embed.timestamp?.let { formatter.parse(it) }
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
                } catch (e: Exception) {
                    it.hook.editMessage(content = "Failed to resolve your message content\n```fix\n${e.stackTraceToString()}```").queue()
                }
            }

            "say" -> {
                val message = it.getOption("message")?.asString ?: return
                it.channel.send(message).queue()
                it.reply_("Message sent!", ephemeral = true).queue()
            }

            "components" -> {
                it.reply_("Moved to Message Apps -> Edit Components", ephemeral = true).queue()
            }
        }
    }

    override suspend fun triggerMessageApp(it: MessageContextInteractionEvent) {
        val message = it.target
        val components = message.components
    }
}