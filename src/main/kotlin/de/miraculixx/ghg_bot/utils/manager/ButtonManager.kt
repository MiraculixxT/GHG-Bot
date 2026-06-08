package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.modules.notifications.NotifyButtons
import de.miraculixx.ghg_bot.modules.other.TimeoutButton
import de.miraculixx.ghg_bot.modules.tickets.TicketButtonHandler
import de.miraculixx.ghg_bot.modules.user_moderation.ButtonsVote
import de.miraculixx.ghg_bot.modules.user_moderation.ButtonsVoteAdmin
import de.miraculixx.ghg_bot.commands.VerifyCommand
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ButtonManager {
    private val buttons = mapOf(
        "TICKET" to TicketButtonHandler(),
        "NOTIFY" to NotifyButtons(),
        "TIMEOUT" to TimeoutButton(),
        "REPORT" to ButtonsVote(),
        "REPORT-ADMIN" to ButtonsVoteAdmin(),
        "VERIFY" to VerifyCommand
    )

    fun startListen(jda: JDA) = jda.listener<ButtonInteractionEvent> {
        val id = it.button.customId ?: return@listener
        val commandClass = when {
            id.startsWith("TICKET-") -> buttons["TICKET"]
            id.startsWith("NOTIFY") -> buttons["NOTIFY"]
            id.startsWith("TIMEOUT:") -> buttons["TIMEOUT"]
            id.startsWith("REPORT:") -> buttons["REPORT"]
            id.startsWith("REPORT-ADMIN:") -> buttons["REPORT-ADMIN"]
            id.startsWith("VERIFY:") -> buttons["VERIFY"]
            else -> buttons[id]
        }
        commandClass?.trigger(it)
    }
}
