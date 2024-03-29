package de.miraculixx.ghg_bot

import de.miraculixx.ghg_bot.config.ConfigManager
import de.miraculixx.ghg_bot.modules.auto_support.AutoSupportMessages
import de.miraculixx.ghg_bot.modules.auto_support.AutoSupportReactions
import de.miraculixx.ghg_bot.modules.auto_support.TabCompleteEvent
import de.miraculixx.ghg_bot.modules.moderation.SpamCheck
import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.modules.other.CommandOnlyChannel
import de.miraculixx.ghg_bot.modules.other.ThreadChannel
import de.miraculixx.ghg_bot.modules.tickets.TicketMessages
import de.miraculixx.ghg_bot.modules.user_moderation.UserModerationManager
import de.miraculixx.ghg_bot.modules.voice.AlwaysOneFree
import de.miraculixx.ghg_bot.utils.log.Color
import de.miraculixx.ghg_bot.utils.log.consoleChannel
import de.miraculixx.ghg_bot.utils.log.log
import de.miraculixx.ghg_bot.utils.manager.ButtonManager
import de.miraculixx.ghg_bot.utils.manager.DropDownManager
import de.miraculixx.ghg_bot.utils.manager.ModalManager
import de.miraculixx.ghg_bot.utils.manager.SlashCommandManager
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag

fun main() {
    Main()
}

class Main {
    companion object {
        lateinit var JDA: JDA
    }

    private fun command() {
        runBlocking {
            var running = true
            while (running) {
                when (readlnOrNull()) {
                    "exit" -> {
                        running = false
                        ConfigManager.save()
                        Warnings.save()
                        UserModerationManager.save()
                        JDA.shardManager?.setStatus(OnlineStatus.OFFLINE)
                        JDA.shutdown()
                    }

                    "save" -> {
                        ConfigManager.save()
                        Warnings.save()
                        UserModerationManager.save()
                        println("Configs saved!")
                    }

                    "load" -> {
                        ConfigManager.updateRegex()
                        println("Configs updated!")
                    }

                    null -> {}
                    else -> println("Commands: exit, save, load")
                }
            }
        }
    }

    init {
        val dcToken = ConfigManager.credentials.dcToken
        JDA = default(dcToken) {
            enableCache(CacheFlag.VOICE_STATE)
            setActivity(Activity.watching("nach Fragen \uD83D\uDC40"))
            setStatus(OnlineStatus.IDLE)
            intents += listOf(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
            intents -= listOf(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_MESSAGE_TYPING)
            setMemberCachePolicy(MemberCachePolicy.DEFAULT)
        }
        JDA.awaitReady()
        consoleChannel = JDA.getTextChannelById(1036256164284997773)!!

        SlashCommandManager.startListen(JDA)
        ButtonManager.startListen(JDA)
        DropDownManager.startListen(JDA)
        ModalManager.startListen(JDA)
        TabCompleteEvent()
        Warnings
        ThreadChannel
        AlwaysOneFree
        CommandOnlyChannel

        AutoSupportMessages()
        AutoSupportReactions()
        TicketMessages()
        SpamCheck()

        "GHG Bot is now online!".log(Color.GREEN)

        command()
    }
}

val JDA by lazy { Main.JDA }