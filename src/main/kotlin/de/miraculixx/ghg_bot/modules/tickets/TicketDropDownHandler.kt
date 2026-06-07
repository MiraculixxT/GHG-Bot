package de.miraculixx.ghg_bot.modules.tickets

import de.miraculixx.ghg_bot.utils.cache.embedAd
import de.miraculixx.ghg_bot.utils.cache.embedApplication
import de.miraculixx.ghg_bot.utils.entities.DropDownEvent
import dev.minn.jda.ktx.interactions.components.EntitySelectMenu
import dev.minn.jda.ktx.interactions.components.Label
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.TextInput
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.components.ModalTopLevelComponent
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.label.LabelChildComponent
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.modals.Modal

class TicketDropDownHandler : DropDownEvent {
    private val reportInfo = Embed {
        title = "⚠️ Warte!"
        description = "Bevor du einen Nutzer melden möchtest, wollen wir dich an folgendes erinnern:\n" +
                "\n" +
                "**Nutzer melden**\n" +
                "> - Halte den Nutzer Tag (ID) bereit -> `@name`\n" +
                "> - Halte einen Beweis bereit -> Bilder oder Videos (nur `mp4`)\n" +
                "\n" +
                "Klicke auf den **roten Knopf** unter dieser Nachricht, um einen Nutzer zu melden!"
        color = 0xcc0000
    }
    private val reportButton = button("TICKET-REPORT", "Nutzer Melden", Emoji.fromFormatted("\uD83D\uDD28"), style = ButtonStyle.DANGER)

    private val unbanInfo = Embed {
        title = "Unban Request"
        description = "Du möchtest einen Account entsperren lassen? Drücke hierfür auf den Knopf unter dieser Nachricht und fülle das Formular aus. " +
                "Nehme dir dafür genug Zeit, nur eine Anfrage pro Account ist möglich!\n" +
                "\n" +
                "**Wichtig**\n" +
                "> Zweit Accounts sind verboten! Sollte dein Hauptaccount gesperrt worden sein, ist die Nutzung dieses aktuellen Accounts verboten! Wir überprüfen dies Regelmäßig."
        color = 0xcc0000
    }
    private val unbanButton = button("https://appeal.gg/ghg", "Unban Formular", Emoji.fromFormatted("\uD83D\uDD13"), style = ButtonStyle.LINK)

    override suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {
        val selected = it.values.firstOrNull()
        println(selected)
        when (selected) {
            "WERBUNG" -> it.reply_(embeds = listOf(embedAd), ephemeral = true).queue()
            "BEWERBUNG" -> it.reply_(embeds = listOf(embedApplication), ephemeral = true).queue()
            "UNBAN" -> it.reply_(embeds = listOf(unbanInfo), components = listOf(ActionRow.of(unbanButton)), ephemeral = true).queue()
            "MELDEN" -> it.replyModal("TICKET-REPORT", "Nutzer melden") {
                label("Der Nutzer") {
                    child = EntitySelectMenu("USER", listOf(EntitySelectMenu.SelectTarget.USER))
                }

                label("Grund & Nachweis") {
                    description = "Erkläre so gut wie möglich, wie der Nutzer die Regeln bricht! (Datei Nachweise ins Ticket hochladen)"
                    child = TextInput("CONTENT", TextInputStyle.PARAGRAPH, placeholder = "Der Nutzer macht...", requiredLength = 50..2000)
                }

                label("Regelbruch Art") {
                    child = StringSelectMenu("TYPE") {
                        option("Scam / Steam Report / Phishing", "SPAM", emoji = Emoji.fromFormatted("\uD83C\uDF10"))
                        option("Voice Channel", "VOICE", emoji = Emoji.fromFormatted("\uD83C\uDF99\uFE0F"))
                        option("Server Chat", "CHAT", emoji = Emoji.fromFormatted("\uD83D\uDCDD"))
                        option("Sonstiges", "OTHER", "Reports in dieser Kategorie können länger dauern")
                    }
                }

                label("Nachweis Vorhanden?") {
                    child = StringSelectMenu("EVIDENCE") {
                        option("Ich hab einen Link beigelegt oder lade es gleich ins Ticket!", "YES", "Voice Channel: Video - Text/User: Screenshots", emoji = Emoji.fromFormatted("✅"))
                        option("Nein, habe nichts", "NO", "Leider können wir ohne Nachweise nicht viel machen. Andere Nutzer als Zeugen zählen nicht", emoji = Emoji.fromFormatted("❌"))
                    }
                }
            }.queue()
            "SONSTIGES" -> it.replyModal("TICKET-OTHER", "Neues Ticket") {
                label("Deine Nachricht") {
                    child = TextInput("CONTENT", TextInputStyle.PARAGRAPH, placeholder = "Mit dieser Nachricht beginnt das Ticket") {
                        required = true
                        requiredLength = 50..2000
                    }
                }
            }.queue()
        }
    }
}