package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class AdminCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val channel = it.messageChannel

        when (it.subcommandName) {
            "create-ticket-panel" -> {
                val ticketData = getTicketPanel()
                channel.sendMessageEmbeds(ticketData.first).addComponents(ActionRow.of(ticketData.second)).queue()
                it.reply_("Wurde gesendet!", ephemeral = true).queue()
            }

            "create-notify-panel" -> {
                val notifyData = getNotifyPanel()
                channel.sendMessageEmbeds(notifyData.first).addComponents(ActionRow.of(notifyData.second)).queue()
                it.reply_("Wurde gesendet!", ephemeral = true).queue()
            }
        }
    }

    private fun getNotifyPanel(): Pair<MessageEmbed, Button> {
        return Embed {
            title = "\uD83D\uDD14 ||  **Twitch & YouTube Pings**"
            description = "<:blanc:1059482494644269056> \n" +
                    "Hier kannst du Ping **Benachrichtigungen** für neue Twitch **Streams** oder YouTube **Videos** aktivieren oder deaktivieren. " +
                    "Klicke dafür auf den :bell: Knopf unter dieser Nachricht!\n" +
                    "\n" +
                    "> <@&604279182872018974> -> YouTube **Hauptkanal** Videos\n" +
                    "> <@&604279184038297620> -> YouTube **Zweitkanal** Videos\n" +
                    "> <@&604277191823589396> -> Twitch **Streams**"
            color = 0xb800ff
        } to button("NOTIFY", "Einstellungen ändern", Emoji.fromFormatted("\uD83D\uDD14"), ButtonStyle.PRIMARY)
    }

    private fun getTicketPanel(): Pair<MessageEmbed, StringSelectMenu> {
        return Embed {
            title = "⁉️ ||  **Fragen und Meldungen**"
            description = "Hier kannst du Tickets öffnen um dem Team private **Fragen** zu stellen oder **Nutzer melden**!\n" +
                    "\n" +
                    "**Nutzer melden**\n" +
                    "> - Halte den Nutzer Tag (ID) bereit -> `name#0000`\n" +
                    "> - Halte einen Beweis bereit -> Bilder oder Videos (nur `mp4`)\n" +
                    "\n" +
                    "**Achtung**\n" +
                    "> Missbrauch des Ticketsystems wird zum **Ausschluss** gebracht!\n" +
                    "> Wähle ein **passendes** Thema zu deinem Anliegen im unteren Menü aus um Hilfe zu erhalten.\n" +
                    "\n" +
                    "Bereit? Dann wähle ein Thema aus dem unteren Menü aus um ein neues Ticket zu öffnen!"
            url = "https://discord.com/channels/484676017513037844/859833491251789844"
            color = 0xb800ff
        } to StringSelectMenu("TICKET") {
            placeholder = "Wähle ein Thema für dein Anliegen"
            maxValues = 1
            minValues = 1
            option("DM Werbung", "WERBUNG", "Jegliche Werbung unbekannter Accounts in DMs", Emoji.fromFormatted("\uD83C\uDF9F️"))
            option("Bewerbung", "BEWERBUNG", "Bewerbungen für Twitch/Discord/Dev", Emoji.fromFormatted("\uD83D\uDEE1️"))
            option("Nutzer melden", "MELDEN", "Melde Nutzer für Vergehen auf diesem Discord", Emoji.fromFormatted("\uD83D\uDD28"))
            option("Unban Anfrage", "UNBAN", "Stelle eine unban Anfrage für einen Account", Emoji.fromFormatted("\uD83D\uDD13"))
            option("Sonstiges", "SONSTIGES", "Anliegen ausserhalb der oberen Themen", Emoji.fromFormatted("❔"))
        }
    }
}