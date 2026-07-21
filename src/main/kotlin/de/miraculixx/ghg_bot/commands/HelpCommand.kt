package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.config.ConfigManager
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import de.miraculixx.ghg_bot.utils.log.noGuild
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object HelpCommand : SlashCommandEvent {
    // <name, content>
    val presets = ConfigManager.moderation.supportPresets

    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        if (it.guild == null) {
            it.reply_(noGuild).queue()
            return
        }

        when (it.subcommandName) {
            "send" -> {
                val name = it.getOption("name")?.asString ?: return
                val content = presets[name]
                if (content == null) {
                    it.reply_("Kein Preset mit dem Namen `$name` gefunden!", ephemeral = true).queue()
                    return
                }
                val ping = it.getOption("ping")?.asMember
                it.reply_("${if (ping != null) "${ping.asMention}\n" else ""}$content").queue()
            }

            "new" -> {
                val name = it.getOption("name")?.asString ?: return
                val msgID = it.getOption("id")?.asLong ?: return
                val msg = it.channel.retrieveMessageById(msgID).await()
                val content = msg.contentRaw
                presets[name] = content
                ConfigManager.save()
                it.reply_("Preset `$name` erfolgreich erstellt mit folgendem Inhalt!\n\n$content", ephemeral = true).queue()
            }

            "remove" -> {
                val name = it.getOption("name")?.asString ?: return
                val removed = presets.remove(name)
                if (removed == null) {
                    it.reply_("Kein Preset mit dem Namen `$name` gefunden!", ephemeral = true).queue()
                    return
                }
                ConfigManager.save()
                it.reply_("Preset `$name` erfolgreich entfernt!", ephemeral = true).queue()
            }
        }
    }
}