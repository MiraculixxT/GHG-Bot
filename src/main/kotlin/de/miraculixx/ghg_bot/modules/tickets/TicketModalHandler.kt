package de.miraculixx.ghg_bot.modules.tickets

import de.miraculixx.ghg_bot.utils.cache.ticketRole
import de.miraculixx.ghg_bot.utils.entities.ModalEvent
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class TicketModalHandler : ModalEvent {
    private val buttonCloseReport = button("TICKET-CLOSE-REPORT", "Schließen", Emoji.fromFormatted("\uD83D\uDD12"), ButtonStyle.DANGER)
    private val buttonCloseOther = button("TICKET-CLOSE", "Schließen", Emoji.fromFormatted("\uD83D\uDD12"), ButtonStyle.DANGER)

    override suspend fun trigger(it: ModalInteractionEvent) {
        val member = it.member ?: return
        val message = it.getValue("CONTENT")?.asString ?: "Ein Fehler ist aufgetreten..."
        it.deferReply(true).queue()
        val hook = it.hook
        val channel = it.channel
        if (channel !is TextChannel) return

        when (it.modalId) {
            "TICKET-REPORT" -> member.createTicket(TicketType.REPORT, message, hook, it.getValue("TAG")?.asString, it.getValue("ID")?.asString, channel)
            "TICKET-OTHER" -> member.createTicket(TicketType.OTHER, message, hook, channel = channel)

            else -> hook.editMessage(content = "```diff\n- Ein Fehler ist aufgetreten! Bitte melde dies an einen Moderator```").queue()
        }
    }

    private fun Member.createTicket(type: TicketType, message: String, source: InteractionHook, tag: String? = null, id: String? = null, channel: TextChannel) {
        CoroutineScope(Dispatchers.Default).launch {
            val thread = channel.createThreadChannel(type.prefix + user.name, true)
                .setInvitable(false).complete()
            thread.send("${ticketRole.asMention}-${this@createTicket.id}-${this@createTicket.asMention}").complete()
            when (type) {
                TicketType.REPORT -> thread.setupReportTicket(this@createTicket, tag, id, message)
                TicketType.OTHER -> thread.setupOtherTicket(this@createTicket, message)
            }
            source.editMessage(content = "", embeds = listOf(Embed {
                title = "Neues Ticket"
                description = "Dein erstelltes Ticket findest du hier: ${thread.asMention}\n" +
                        "Dort erhältst du Antworten oder kannst noch mehr schreiben"
                color = 0xb800ff
            })).queue()
        }
    }

    private fun ThreadChannel.setupOtherTicket(opener: Member, message: String) {
        send(embeds = listOf(Embed {
            title = "❔  || **Sonstige Frage**"
            description = "Willkommen **${opener.nickname ?: opener.user.name}**!\n" +

                    "\nHier kannst du **privat** mit dem Team in Kontakt treten. " +
                    "Bitte schildere uns dein Anliegen so gut wie möglich, damit wir dir helfen können!" +

                    "\n\n```fix\n- Je nach Zeitpunkt und Anfragen Menge kann es etwas dauern, bis wir dir antworten können." +
                    "\n- Ein Missbrauch des Ticketsystems führt zum Ausschluss!```"
            color = 0xb800ff
        }), components = listOf(ActionRow.of(buttonCloseOther))).queue()
        send(message).queue()
    }

    private fun ThreadChannel.setupReportTicket(opener: Member, tag: String?, id: String?, message: String) {
        send(embeds = listOf(Embed {
            title = "\uD83D\uDD28  || **Nutzer Melden**"
            description = "Willkommen **${opener.nickname ?: opener.user.name}**!\n" +

                    "\nHier kannst du **Nutzer melden**, welche gegen unsere <#484677070522417162> verstoßen!\n" +
                    "\n**__User Reports__**" +
                    "\n> - Nutzer **Tag** -> `${tag ?: "Nicht angegeben"}`" +
                    "\n> - Nutzer **ID** -> `${id?.ifBlank { "Nicht angegeben" } ?: "Nicht angegeben"}`" +
                    "\n> - **Beweis** -> Bilder oder Videos (nur `mp4`)" +

                    "\n\n```fix\n- Je nach Zeitpunkt und Anfragen Menge kann es etwas dauern, bis wir dir antworten können." +
                    "\n- Ein Missbrauch des Ticketsystems führt zum Ausschluss!```"
            color = 0xb800ff
        }), components = listOf(ActionRow.of(buttonCloseReport))).queue()
        send(message).queue()
    }
}