package de.miraculixx.ghg_bot.utils.manager

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.commands.*
import de.miraculixx.ghg_bot.modules.auto_support.SupportFilter
import de.miraculixx.ghg_bot.utils.log.log
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object SlashCommandManager {
    private val commands = mapOf(
        "auto-support" to AutoModCommand(),
        "support" to HelpCommand(),
        "admin" to AdminCommand(),
        "just-google" to LetMeGoogleCommand(),
        "warnings" to WarnCommand(),
        "stream-commands" to StreamCommands(),
        "report" to ReportCommand(),
        "reputation" to UserModerationCommand(),
        "message" to MessageCommand(),
    )
    private val streamCommands = setOf("tastatur", "maus", "socials", "merch", "monitor", "mauspad", "mc-settings", "pc-specs", "timolia")

    fun startListen(jda: JDA) = jda.listener<SlashCommandInteractionEvent> {
        val commandClass = commands[it.name] ?: (if (streamCommands.contains(it.name)) commands["stream-commands"] else null) ?: return@listener
        val options = buildString { it.options.forEach { option -> append(option.asString + " ") } }
        ">> ${it.user.asTag} -> /${it.name}${it.subcommandName ?: ""} $options".log()
        commandClass.trigger(it)
    }

    private val onUserApp = JDA.listener<UserContextInteractionEvent> {
        val commandClass = when (it.name) {
            "Melde Nutzer" -> commands["report"]
            else -> commands[it.name]
        } ?: return@listener
        commandClass.triggerUserApp(it)
    }

    private val onMessageApp = JDA.listener<MessageContextInteractionEvent> {
        val commandClass = when (it.name) {
            "Melde Nachricht" -> commands["report"]
            else -> commands[it.name]
        } ?: return@listener
        commandClass.triggerMessageApp(it)
    }

    init {
        //Implement all Commands into Discord

        JDA.guilds.forEach {
            if (it.idLong == 484676017513037844 || it.idLong == 989881712492298250) return@forEach
            println("Guild Info: ${it.idLong} - ${it.name} || OwnerID: ${it.retrieveOwner().complete().idLong}")
            it.leave().queue()
        }

        "Guilds - ${JDA.guilds.size}".log()
        JDA.updateCommands().addCommands(
            Command("auto-support", "Setup the regex for auto support messages") {
                defaultPermissions = DefaultMemberPermissions.DISABLED
                subcommand("list", "Zeige alle Filter Events") {
                    option<String>("filter", "Welchen Filter?", true) {
                        SupportFilter.entries.forEach { filter ->
                            choice(filter.name, filter.name)
                        }
                    }
                }
                subcommand("add", "Füge ein neuen Filter hinzu") {
                    option<String>("filter", "Welchen Filter?", true) {
                        SupportFilter.entries.forEach { filter ->
                            choice(filter.name, filter.name)
                        }
                    }
                    option<String>("key", "Was hinzugefügt werden soll", true)
                }
                subcommand("remove", "Entferne einen aktuellen Filter") {
                    option<String>("filter", "Welchen Filter?", true) {
                        SupportFilter.entries.forEach { filter ->
                            choice(filter.name, filter.name)
                        }
                    }
                    option<String>("key", "Was entfernt werden soll", true, true)
                }
            },
            Command("support", "Sende eine Support Nachricht") {
                defaultPermissions = DefaultMemberPermissions.DISABLED
                option<String>("type", "Welche Support Nachricht?", true) {
                    SupportFilter.entries.forEach { filter ->
                        choice(filter.name, filter.name)
                    }
                }
                option<Member>("ping", "Pinge einen User")
            },
            Command("admin", "Bearbeite Tickets") {
                subcommand("create-ticket-panel", "Erstelle den Ticket Entrypoint") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                }
                subcommand("create-notify-panel", "Erstelle das Notification Panel") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                }
                subcommand("timeout-selection", "Erstelle ein fun timeout panel") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                }
                subcommand("clear-threads", "Lösche alle threads") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                }
                subcommand("vote-info", "Printe vote info") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                }
            },
            Command("just-google", "Erzeugt einen Let-Me-Google-That Link") {
                defaultPermissions = DefaultMemberPermissions.DISABLED
                option<String>("prompt", "Was soll gegoogelt werden", true)
                addOption(OptionType.USER, "ping", "Wer soll gepingt werden", false)
            },
            Command("warnings", "Verwalte Warnungen") {
                subcommand("amount", "Erhalte die Menge an Warnungen") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                    addOption(OptionType.USER, "user", "Welcher Nutzer?", true)
                }
                subcommand("warn", "Warne einen Nutzer") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                    addOption(OptionType.USER, "user", "Welcher Nutzer?", true)
                    option<String>("reason", "Warum?", true)
                }
                subcommand("set-warns", "Ändere Warnungsanzahl") {
                    defaultPermissions = DefaultMemberPermissions.DISABLED
                    addOption(OptionType.USER, "user", "Welcher Nutzer?", true)
                    option<Int>("amount", "Wie viele?", true)
                }
            },
            Command("reputation", "Manage User Reputation") {
                defaultPermissions = DefaultMemberPermissions.DISABLED
                subcommand("add", "Add Reputation to a User") {
                    addOption(OptionType.USER, "user", "Welcher Nutzer?", true)
                    option<Int>("points", "Wie viele Punkte?", true)
                }
                subcommand("remove", "Remove Reputation from a User") {
                    addOption(OptionType.USER, "user", "Welcher Nutzer?", true)
                    option<Int>("points", "Wie viele Punkte?", true)
                }
                subcommand("get", "Get Reputation of a User") {
                    addOption(OptionType.USER, "user", "Welcher Nutzer?", true)
                }
            },
            Command("message", "Send and handle bot messages") {
                subcommand("say", "Send a simple message") {
                    option<String>("message", "What should be sent?", true)
                }
                subcommand("say-json", "Send a complex message from https://discohook.org") {
                    option<String>("json", "The JSON context", true)
                    addOption(OptionType.ATTACHMENT, "upload", "The file to upload", false)
                }
            },
            *streamCommands.map {
                Command(it, "Informationen über Basti's $it")
            }.toTypedArray(),
            Commands.user("Melde Nutzer").setGuildOnly(true),
            Commands.message("Melde Nachricht").setGuildOnly(true),
            Commands.user("warnings").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.DISABLED),
        ).queue()
    }
}