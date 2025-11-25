package de.miraculixx.ghg_bot.modules.tickets

import de.miraculixx.ghg_bot.utils.cache.embedAd
import de.miraculixx.ghg_bot.utils.cache.embedApplication
import de.miraculixx.ghg_bot.utils.entities.DropDownEvent
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class TicketDropDownHandler : DropDownEvent {
    private val reportInfo = Embed {
        title = "⚠️ Warte!"
        description = "Bevor du einen Nutzer melden möchtest, wollen wir dich an folgendes erinnern:\n" +
                "\n" +
                "**Nutzer melden**\n" +
                "> - Halte den Nutzer Tag (ID) bereit -> `name#0000`\n" +
                "> - Halte einen Beweis bereit -> Bilder oder Videos (nur `mp4`)\n" +
                "\n" +
                "Klicke auf den **roten Knopf** unter dieser Nachricht, um einen Nutzer zu melden!"
        color = 0xcc0000
    }
    private val reportButton = button("TICKET-REPORT", "Nutzer Melden", Emoji.fromFormatted("\uD83D\uDD28"), ButtonStyle.DANGER)

    private val unbanInfo = Embed {
        title = "Unban Request"
        description = "Du möchtest einen Account entsperren lassen? Drücke hierfür auf den Knopf unter dieser Nachricht und fülle das Formular aus. " +
                "Nehme dir dafür genug Zeit, nur eine Anfrage pro Account ist möglich!\n" +
                "\n" +
                "**Wichtig**\n" +
                "> Zweit Accounts sind verboten! Sollte dein Hauptaccount gesperrt worden sein, ist die Nutzung dieses aktuellen Accounts verboten! Wir überprüfen dies Regelmäßig."
        color = 0xcc0000
    }
    private val unbanButton = button("https://appeal.gg/ghg", "Unban Formular", Emoji.fromFormatted("\uD83D\uDD13"), ButtonStyle.LINK)

    override suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {
        val selected = it.values.firstOrNull()
        println(selected)
        when (selected) {
            "WERBUNG" -> it.reply_(embeds = listOf(embedAd), ephemeral = true).queue()
            "BEWERBUNG" -> it.reply_(embeds = listOf(embedApplication), ephemeral = true).queue()
            "MELDEN" -> it.reply_(embeds = listOf(reportInfo), components = listOf(ActionRow.of(reportButton)), ephemeral = true).queue()
            "UNBAN" -> it.reply_(embeds = listOf(unbanInfo), components = listOf(ActionRow.of(unbanButton)), ephemeral = true).queue()
            "SONSTIGES" -> it.replyModal("TICKET-OTHER", "Neues Ticket") {
                paragraph("CONTENT", "Deine Nachricht", true, null, "Mit dieser Nachricht beginnt das Ticket") {
                    minLength = 50
                    maxLength = 2000
                }
            }.queue()
        }
    }
}