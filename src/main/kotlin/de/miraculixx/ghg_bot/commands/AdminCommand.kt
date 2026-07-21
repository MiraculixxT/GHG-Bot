package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.Main
import de.miraculixx.ghg_bot.utils.cache.guildGHG
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import de.miraculixx.ghg_bot.utils.extensions.json
import de.miraculixx.ghg_bot.utils.log.LOGGER
import de.miraculixx.ghg_bot.utils.log.noGuild
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class AdminCommand : SlashCommandEvent {

    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        if (it.guild == null) {
            it.reply_(noGuild).queue()
            return
        }
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

            "timeout-selection" -> {
                val timeoutData = getTimeoutSelection()
                it.reply_(embeds = listOf(timeoutData.first), components = listOf(ActionRow.of(timeoutData.second)))
                    .queue()
            }

            "clear-threads" -> {
                (it.guildChannel as TextChannel).threadChannels.forEach { thread -> thread.delete().queue() }
            }

            "vote-info" -> {
                it.channel.send(
                    "## Wie funktioniert das Report-System? <:ban:742347113538781224> \n" +
                            "\n" +
                            "1. **Report-Kanal:** Gehe zum <#1193169356239163432>-Channel, um die aktuellen **Reports** zu sehen und abzustimmen. Klicke auf den unteren **Button** um mit zu machen!\n" +
                            "\n" +
                            "2. **Melden von Nachrichten:** Du kannst Nachrichten melden, indem du mit der rechten Maustaste auf die **Nachricht** klickst, dann auf **Apps** gehst und ``Nachricht melden`` auswählst. Auf mobile drücke länger auf die Nachricht und dann auf Apps.\n" +
                            "\n" +
                            "3. **Abstimmen:** Nutze die Buttons unter jedem aktiven Report, um abzustimmen.\n" +
                            "\n" +
                            "## Warum ist richtiges Voten wichtig? <:baseg:1062108033288786072>\n" +
                            "\n" +
                            "Wenn du konsequent korrekt und zuverlässig votest, baust du Vertrauen auf. Vertrauenswürdige Mitglieder haben die Möglichkeit, eine größere Rolle bei Entscheidungen zu spielen und helfen, den Server für alle angenehm zu gestalten <:PeepoGlad:1062106500056743947> \n" +
                            "\n" +
                            "*Dieses System ist noch in der Beta, bei Fragen oder Problemen bitte an <@341998118574751745> wenden*",
                    components = listOf(
                        ActionRow.of(
                            success("REPORT-ADMIN:OPTIN", "Teilnehmen"),
                            secondary("REPORT-ADMIN:POINTS", "Erhaltene Punkte")
                        )
                    )
                ).queue()
            }

            "prune-sus-member" -> {
                val msg = it.reply_("Searching for sussy members...").await()
                val response =
                    Main.ktorClient.post("https://discord.com/api/v9/guilds/484676017513037844/members-search") {
                        header("Authorization", JDA.token)
                        header("Content-Type", "application/json")
                        setBody("{\"or_query\": {   \"safety_signals\": {    \"unusual_account_activity\":true   }  },  \"and_query\":{},  \"limit\":1000 }")
                    }
                LOGGER.info("Sussy member search response: ${response.status.value} ${response.status.description}")
                val rawResp = response.bodyAsText()
                LOGGER.debug("Sussy member search raw body (first 100 chars): ${rawResp.take(100)}...")
                val susMembers = try {
                    json.decodeFromString<UserLookUp>(rawResp)
                } catch (e: Exception) {
                    e.printStackTrace()
                    msg.editMessage(content = "Failed to parse response: ${e.message}").queue()
                    return
                }

                LOGGER.info("Parsed ${susMembers.members.size} members with suspicious signals")
                val first = susMembers.members.firstOrNull()
                val last = susMembers.members.lastOrNull()
                if (first == null || last == null) {
                    msg.editMessage(content = "No sussy members found.").queue()
                    return
                }

                val accept = JDA.button(label = "Confirm", style = ButtonStyle.SUCCESS) { b ->
                    try {
                        b.editMessage("Pruning ${susMembers.members.size} sussy members...")
                            .setComponents(ActionRow.of(b.button.asDisabled())).queue()
                        val members = guildGHG.loadMembers().await()
                        LOGGER.info("Loaded ${members.size} guild members for sussy prune")
                        val users = susMembers.members.mapNotNull {
                            try {
                                LOGGER.debug("Resolving sussy member ${it.member.user.id} (${it.member.user.username})")
                                guildGHG.getMemberById(it.member.user.id)
                                    ?: guildGHG.retrieveMember(UserSnowflake.fromId(it.member.user.id)).await()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                        pruneMembers(users, msg)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        msg.editMessage(content = "Failed to retrieve members: ${e.message}").queue()
                        return@button
                    }
                }
                LOGGER.debug("Sending sussy-member prune confirmation prompt")
                msg.editMessage(
                    content = "Found ${susMembers.members.size} sussy members.\n- From: <@${first.member.user.id}> (${first.member.joined_at})\n- To: <@${last.member.user.id}> (${last.member.joined_at})",
                    components = listOf(
                        ActionRow.of(accept)
                    )
                ).queue()
            }
        }
    }

    private suspend fun pruneMembers(members: List<Member>, event: InteractionHook) = coroutineScope {
        val total = members.size
        val count = AtomicInteger(0)
        val embed = Embed {
            title = "<:ghg:1059233059532197979> || Potentieller Spam Entdeckt"
            description =
                "Dein Account wurde als **potentieller Spam Account** erkannt und wird temporär von dem BastiGHG-Server entfernt. \n\n" +
                        "> Solltest du kein Spam Account sein ändere bitte umgehend dein __**Passwort**__ und aktiviere __**2FA**__!\n\n" +
                        "Dein Account wird in **1 Stunde** automatisch entsperrt, danach kannst du dem Server wieder beitreten. " +
                        "Sollte du nicht automatisch entsperrt werden, erstelle einen Antrag auf https://appeal.gg/ghg"
            color = 0xff0000
        }

        LOGGER.info("Start pruning ${members.size} suspicious members...")
        val snowflakes = members.map { UserSnowflake.fromId(it.user.id) }
        LOGGER.debug("Prepared ${snowflakes.size} snowflakes for unban scheduling")
        try {
            File("prune_sus_members.json").writeText(json.encodeToString(members.map { it.id }))
        } catch (e: Exception) {
            e.printStackTrace()
            return@coroutineScope
        }
        launch {
            while (count.get() < total) {
                LOGGER.info("Pruning progress: ${count.get()} / $total members")
                event.editMessage(
                    content = "Pruning **${count.get()} / $total** sussy members..."
                ).queue()
                delay(3.seconds)
            }
            event.editMessage(content = "Pruning complete! ($total member)\nThey can join in 1h again").queue()

            CoroutineScope(Dispatchers.Default).launch {
                delay(1.hours)
                snowflakes.forEach {
                    runCatching {
                        guildGHG.unban(it).queue()
                        LOGGER.info("Unbanned pruned member ${it.id}")
                    }
                }
            }

            return@launch
        }

        // execute for each member
        members.map { member ->
            launch {
                try {
                    member.user.openPrivateChannel().flatMap { channel ->
                        channel.send("https://discord.gg/ghg", embeds = listOf(embed))
                    }.await()
                    val id = member.id
                    member.ban(1, TimeUnit.SECONDS).await()
                    LOGGER.info("Banned suspicious member $id")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                count.incrementAndGet()
            }
        }
    }

    private fun getTimeoutSelection(): Pair<MessageEmbed, List<Button>> {
        return Embed {
            title = "**Kostenlose Timeouts**"
            description =
                "Hier kannst du dir deinen **kostenlosen** Timeout abholen! \nEinfach auf einen Button unten drücken, der dich am meisten interessiert. \n" +
                        "Geschenke werden direkt und unwiderruflich ausgehändigt."
        } to listOf(
            button("TIMEOUT:1", "1h Timeout", style = ButtonStyle.DANGER),
            button("TIMEOUT:2", "1d Timeout", style = ButtonStyle.DANGER),
            button("TIMEOUT:3", "Timeout Entferner", style = ButtonStyle.SUCCESS)
        )
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
        } to button("NOTIFY", "Einstellungen ändern", Emoji.fromFormatted("\uD83D\uDD14"), style = ButtonStyle.PRIMARY)
    }

    private fun getTicketPanel(): Pair<MessageEmbed, StringSelectMenu> {
        return Embed {
            title = "⁉️ ||  **Fragen und Meldungen**"
            description =
                "Hier kannst du Tickets öffnen um dem Team private **Fragen** zu stellen oder **Nutzer melden**!\n" +
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
            option(
                "DM Werbung / Scams",
                "WERBUNG",
                "Jegliche Werbung oder Scams unbekannter Accounts in DMs",
                Emoji.fromFormatted("\uD83C\uDF9F️")
            )
            option("Bewerbung", "BEWERBUNG", "Bewerbungen für Twitch/Discord/Dev", Emoji.fromFormatted("\uD83D\uDEE1️"))
            option(
                "Nutzer melden",
                "MELDEN",
                "Melde Nutzer für Vergehen auf diesem Discord",
                Emoji.fromFormatted("\uD83D\uDD28")
            )
            option(
                "Unban Anfrage",
                "UNBAN",
                "Stelle eine unban Anfrage für einen Account",
                Emoji.fromFormatted("\uD83D\uDD13")
            )
            option("Sonstiges", "SONSTIGES", "Anliegen ausserhalb der oberen Themen", Emoji.fromFormatted("❔"))
        }
    }

    @Serializable
    private data class UserLookUp(
        val guild_id: String,
        val members: List<MemberObject>
    )

    @Serializable
    private data class MemberObject(
        val member: MemberObjectDirect
    )

    @Serializable
    private data class MemberObjectDirect(
        val joined_at: String,
        val user: UserObject
    )

    @Serializable
    private data class UserObject(
        val id: String,
        val username: String
    )
}