package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.auto_support.SupportFilter
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import de.miraculixx.ghg_bot.utils.extensions.enumOf
import de.miraculixx.ghg_bot.utils.extensions.json
import de.miraculixx.ghg_bot.utils.extensions.loadConfig
import de.miraculixx.ghg_bot.utils.log.noGuild
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.serialization.encodeToString
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.io.File

object HelpCommand : SlashCommandEvent {
    private val presetFile = File("config/presets.json")
    // <name, content>
    val presets: MutableMap<String, String> = presetFile.loadConfig(mutableMapOf())

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
                it.reply_("Preset `$name` erfolgreich erstellt mit folgendem Inhalt!\n\n$content", ephemeral = true).queue()
            }

            "remove" -> {
                val name = it.getOption("name")?.asString ?: return
                val removed = presets.remove(name)
                if (removed == null) {
                    it.reply_("Kein Preset mit dem Namen `$name` gefunden!", ephemeral = true).queue()
                    return
                }
                it.reply_("Preset `$name` erfolgreich entfernt!", ephemeral = true).queue()
            }
        }
    }

    fun save() {
        presetFile.writeText(json.encodeToString(presets))
    }
}