package de.miraculixx.ghg_bot.modules.tickets

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.cache.ticketQuestionRole
import de.miraculixx.ghg_bot.utils.cache.ticketReportRole
import de.miraculixx.ghg_bot.utils.entities.ModalEvent
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Thumbnail
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
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.InteractionHook

object TicketModalHandler : ModalEvent {
    private val buttonCloseReport = button("TICKET-CLOSE-REPORT", "Schließen", Emoji.fromFormatted("\uD83D\uDD12"), style = ButtonStyle.DANGER)
    private val buttonCloseOther = button("TICKET-CLOSE", "Schließen", Emoji.fromFormatted("\uD83D\uDD12"), style = ButtonStyle.DANGER)
    private val reportChannel = JDA.getChannel<TextChannel>(859833491251789844)!!

    override suspend fun trigger(it: ModalInteractionEvent) {
        val member = it.member ?: return
        val message = it.getValue("CONTENT")?.asString ?: "Ein Fehler ist aufgetreten..."
        it.deferReply(true).queue()
        val hook = it.hook
        val channel = it.channel
        if (channel !is TextChannel) return
        println("Modal: ${it.modalId} by ${member.user.name} in channel ${channel.name}")

        when (it.modalId) {
            "TICKET-REPORT" -> {
                val type = it.getValue("TYPE")?.asStringList?.firstOrNull() ?: "Nicht angegeben"
                if (type == "SPAM") {
                    hook.editMessage(content = "## Vielen Dank für die Meldung\n" +
                            "Bitte melde den Nutzer ebenfalls an Discord als Spam/Scam, damit der Platform weit gesperrt werden kann.\n" +
                            "Interagiere **nicht** mit solchen Accounts, diese sind meist nur Bots. Wechsel deine Anmeldedaten **sofort**, solltest du etwas weitergegeben haben!").queue()
                    return
                }
                val reported = it.getValue("USER")?.asMentions?.members?.firstOrNull()
                if (reported == null) {
                    hook.editMessage(content = "```diff\n- Der Nutzer muss auf diesem Server sein, damit du ihn melden kannst!```").queue()
                    return
                }
                val evidence = it.getValue("EVIDENCE")?.asStringList?.firstOrNull() == "YES"
                if (!evidence) {
                    hook.editMessage(content = "```diff\n- Ein Beweis wird benötigt!\n- Voice Channel: Video Aufnahme\n- Text/User: Screenshot```").queue()
                    return
                }
                member.createTicket(TicketType.REPORT, hook) { thread ->
                    thread.setupReportTicket(member, reported, message, type)
                }
            }

            "TICKET-OTHER" -> member.createTicket(TicketType.OTHER, hook) { thread ->
                thread.setupOtherTicket(member, message)
            }

            else -> hook.editMessage(content = "```diff\n- Ein Fehler ist aufgetreten! Bitte melde dies an einen Moderator```").queue()
        }
    }

    fun Member.createTicket(type: TicketType, source: InteractionHook, setup: (ThreadChannel) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            val thread = reportChannel.createThreadChannel(type.prefix + user.name, true)
                .setInvitable(false).complete()
            val ping = when (type) {
                TicketType.OTHER -> ticketQuestionRole.asMention
                TicketType.REPORT -> ticketReportRole.asMention
            }
            thread.send("${ping}-${this@createTicket.id}-${this@createTicket.asMention}").complete()
            setup(thread)
            source.editMessage(content = "", embeds = listOf(Embed {
                title = "Neues Ticket"
                description = "Dein erstelltes Ticket findest du hier: ${thread.asMention}\n" +
                        "Dort siehst du Antworten vom Team. Reports ohne Nachweise werden geschlossen!"
                color = 0xb800ff
            })).queue()
        }
    }

    private fun ThreadChannel.setupOtherTicket(opener: Member, message: String) {
        send(embeds = listOf(Embed {
            title = "❔  || **Sonstige Frage**"
            description = "Willkommen **${opener.nickname ?: opener.user.name}**!\n" +

                    "\nHier kannst du **privat** mit dem Team in Kontakt treten. " +
                    "Bitte schildere uns dein Anliegen so gut wie möglich, damit wir dir helfen können!"

            footer("Ein Missbrauch des Ticketsystems führt zum Ausschluss!", "https://cdn.discordapp.com/avatars/1036252236151537664/6d4c02fa02a172898a4e84e846b1a635")
            color = 0xb800ff
        }), components = listOf(ActionRow.of(buttonCloseOther))).queue()
        send(message).queue()
    }

    private fun ThreadChannel.setupReportTicket(opener: Member, reported: Member, message: String, type: String) {
        sendMessageComponents(listOf(
            Container {
                text("## \uD83D\uDD28  || Nutzer Melden\nWillkommen **${opener.nickname ?: opener.user.name}**!\n" +
                        "\nÜbersicht deiner **Meldung** gegen ein anderen Nutzer. Sende weitere Nachweise oder Information direkt hier rein!"
                )
                section {
                    accessory = Thumbnail(opener.user.avatarUrl ?: "https://cdn.discordapp.com/avatars/1036252236151537664/6d4c02fa02a172898a4e84e846b1a635")
                    text("## User Report" +
                            "\n> ● **Nutzer** -> ${reported.asMention}" +
                            "\n> <:blanc:1193179205589008455>‣ `${reported.user.name}` - `${reported.id}`" +
                            "\n> ● **Type** -> **$type**"
                    )
                }
            },
            ActionRow.of(buttonCloseReport),
            Container {
                text(":warning: **Beachte, dass das Ticket, wenn du keinen Beweis anhängst, ohne jegliche Nachfrage geschlossen wird! Bei Problemen oder Ähnlichem stehen wir gerne zur Verfügung.**")
                accentColorRaw = 0xE74C3C
            }
        )).useComponentsV2().setAllowedMentions(listOf()).queue()
        send(message).queue()
    }
}