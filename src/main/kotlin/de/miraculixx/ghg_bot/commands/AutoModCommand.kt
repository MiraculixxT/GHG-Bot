package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.config.ConfigManager
import de.miraculixx.ghg_bot.modules.auto_support.SupportFilter
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import de.miraculixx.ghg_bot.utils.extensions.enumOf
import de.miraculixx.ghg_bot.utils.log.noGuild
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AutoModCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        if (it.guild == null) {
            it.reply_(noGuild).queue()
            return
        }
        val filter = it.getOption("filter")?.asString
        val filterEnum = enumOf<SupportFilter>(filter)
        val value = it.getOption("key")?.asString

        when (it.subcommandName) {
            "list" -> it.replyEmbeds(Embed {
                title = "Aktuelle Filter für $filter"
                description = buildString {
                    ConfigManager.regex[filterEnum]?.forEach { s ->
                        println(s)
                        append("- $s\n")
                    }
                }
            }).setEphemeral(true).queue()

            "add" -> {
                filterEnum ?: return
                ConfigManager.regex.getOrPut(filterEnum) {
                    mutableListOf()
                }.add(value ?: return)
                ConfigManager.updateRegex()
                it.reply_("Erfolgreich $value zu $filter hinzugefügt!", ephemeral = true).queue()
            }

            "remove" -> {
                ConfigManager.regex[filterEnum]?.remove(value ?: return)
                ConfigManager.updateRegex()
                it.reply_("Erfolgreich $value von $filter entfernt!", ephemeral = true).queue()
            }

            "send" -> {
                val type = it.getOption("type")?.asString ?: return
                val ping = it.getOption("ping")?.asMember
                val typeEnum = enumOf<SupportFilter>(type) ?: return
                val embed = typeEnum.embed
                it.channel.send(content = ping?.asMention ?: " ",
                    embeds = listOf(
                        Embed {
                            title = embed.title
                            description = embed.description
                            color = embed.colorRaw
                            image = embed.image?.url
                            thumbnail = embed.thumbnail?.url
                        }
                    )).queue()
                it.reply_("Done... Hoffentlich", ephemeral = true).queue()
            }
        }
    }
}