package de.miraculixx.ghg_bot.modules.auto_support

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.config.ConfigManager
import de.miraculixx.ghg_bot.config.Configs
import de.miraculixx.ghg_bot.utils.entities.EventListener
import de.miraculixx.ghg_bot.utils.extensions.enumOf
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

class TabCompleteEvent: EventListener {
    override val listener: CoroutineEventListener = JDA.listener<CommandAutoCompleteInteractionEvent> {
        if (it.name != "auto-support") return@listener
        val filter = it.getOption("filter")?.asString ?: return@listener
        val regexList = ConfigManager.regex[enumOf(filter)] ?: return@listener
        it.replyChoiceStrings(regexList).queue()
    }
}