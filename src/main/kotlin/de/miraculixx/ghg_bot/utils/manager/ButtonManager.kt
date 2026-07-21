package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.modules.notifications.NotifyButtons
import de.miraculixx.ghg_bot.modules.other.TimeoutButton
import de.miraculixx.ghg_bot.modules.tickets.TicketButtonHandler
import de.miraculixx.ghg_bot.modules.user_moderation.ButtonsVote
import de.miraculixx.ghg_bot.commands.VerifyCommand
import de.miraculixx.ghg_bot.commands.QuickMathCommand
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ButtonManager {
    private val buttons = mapOf(
        "TICKET" to TicketButtonHandler(),
        "NOTIFY" to NotifyButtons(),
        "TIMEOUT" to TimeoutButton(),
        "REPORT" to ButtonsVote(),
        "VERIFY" to VerifyCommand,
        "QUICK-MATH" to QuickMathCommand
    )

    fun startListen(jda: JDA) = jda.listener<ButtonInteractionEvent> {
        val id = it.button.customId ?: return@listener
        val commandClass = when {
            id.startsWith("TICKET-") -> buttons["TICKET"]
            id.startsWith("NOTIFY") -> buttons["NOTIFY"]
            id.startsWith("TIMEOUT:") -> buttons["TIMEOUT"]
            id.startsWith("REPORT:") -> buttons["REPORT"]
            id.startsWith("VERIFY:") -> buttons["VERIFY"]
            id == "22142abbf1c74da187fdabd4b59d4456" -> buttons["QUICK-MATH"]
            else -> buttons[id]
        }
        commandClass?.trigger(it)
    }
}
