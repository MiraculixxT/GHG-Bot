package de.miraculixx.ghg_bot.modules.tickets

import de.miraculixx.ghg_bot.utils.cache.*
import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class TicketButtonHandler : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val member = it.member ?: return

        when (it.componentId) {
            "TICKET-REPORT" -> it.replyModal("TICKET-REPORT", "Nutzer Melden") {
                short("TAG", "Nutzer Tag", true, null, member.user.asTag) {
                    minLength = 6
                }
                short("ID", "Nutzer ID", false, null, member.id, 17..20)
                paragraph("CONTENT", "Grund", true, null, "Warum möchtest du den Nutzer melden?") {
                    minLength = 50
                    maxLength = 2000
                }
            }.queue()

            "TICKET-CLOSE" -> {
                it.message.editMessageComponents().queue()
                it.deferReply().queue()
                closeTicket(getChannel(it) ?: return, it.hook, member, "Dein Ticket wurde beantwortet.\nSchaue ins Archiv, solltest du die Antwort nicht mitbekommen haben!")
            }

            "TICKET-CLOSE-REPORT" -> {
                it.message.editMessageComponents().queue()
                it.deferReply().queue()
                closeTicket(getChannel(it) ?: return, it.hook, member, "Vielen Dank für deinen Report!\nWir haben dementsprechend gehandelt.")
            }
        }
    }

    private fun getChannel(event: ButtonInteractionEvent): ThreadChannel? {
        val channel = event.channel as? ThreadChannel
        return if (channel == null) {
            event.hook.editMessage(content = "Der Channel konnte nicht gefunden werden...").queue()
            null
        } else channel
    }

    private fun closeTicket(channel: ThreadChannel, hook: InteractionHook, member: Member, closeMessage: String) {
        hook.editMessage(content = "Ticket wird in 5s geschlossen...").queue()
        channel.getHistoryFromBeginning(100).queue { history ->
            CoroutineScope(Dispatchers.Default).launch {
                val ticketUser = channel.threadMembers.filter {
                    val roles = it.member.roles
                    !roles.contains(ticketQuestionRole) && !roles.contains(ticketReportRole)
                }
                val firstMessage = channel.getHistoryFromBeginning(1).complete().retrievedHistory.firstOrNull()?.contentRaw
                val ownerID = firstMessage?.split('-')?.getOrNull(1)
                val owner = if (ownerID == null) {
                    channel.send("Etwas ist schiefgelaufen beim schließen...").queue()
                    null
                } else guildGHG.getMemberById(ownerID) ?: guildGHG.retrieveMemberById(ownerID).complete()

                val embed = Embed {
                    title = "<:ghg:1059233059532197979>  || Ticket Geschlossen"
                    field(inline = true) {
                        name = "<:opened:1059234825082519632> Geöffnet"
                        value = "${owner?.asMention ?: "Unknown"}\n${owner?.user?.asTag ?: "Unknown"}"
                    }
                    field("<:closed:1059234792094322779> Bearbeitet", "${member.asMention}\n${member.user.asTag}", true)
                    field(inline = true) {
                        name = "<:time:1059234860939620465> Zeit"
                        value = "Geöffnet - <t:${channel.timeCreated.toEpochSecond()}:f>\nGeschlossen - <t:${Instant.now().epochSecond}:f>"
                    }
                    field(inline = false)
                    field {
                        name = "<:reason:1059234889351823421> Information"
                        value = "> Archiv -> ${channel.asMention}\n$closeMessage"
                    }
                    color = 0xb800ff
                }
                ticketArchive.send(embeds = listOf(embed)).queue()
                ticketUser.forEach { user ->
                    user.user.openPrivateChannel().queue {
                        it?.send(embeds = listOf(embed))?.queue()
                    }
                }

                delay(5.seconds)
                channel.manager
                    .setLocked(true)
                    .setArchived(true)
                    .queue()
            }
        }
    }
}