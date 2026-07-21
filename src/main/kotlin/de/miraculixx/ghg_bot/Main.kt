package de.miraculixx.ghg_bot

import de.miraculixx.ghg_bot.commands.HelpCommand
import de.miraculixx.ghg_bot.config.ConfigManager
import de.miraculixx.ghg_bot.modules.auto_support.AutoSupportMessages
import de.miraculixx.ghg_bot.modules.auto_support.AutoSupportReactions
import de.miraculixx.ghg_bot.modules.auto_support.SupportData
import de.miraculixx.ghg_bot.modules.auto_support.TabCompleteEvent
import de.miraculixx.ghg_bot.modules.moderation.SpamCheck
import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.modules.other.FanartHighlighting
import de.miraculixx.ghg_bot.modules.other.ThreadChannel
import de.miraculixx.ghg_bot.modules.tickets.TicketMessages
import de.miraculixx.ghg_bot.modules.voice.AlwaysOneFree
import de.miraculixx.ghg_bot.utils.log.LOGGER
import de.miraculixx.ghg_bot.utils.manager.ButtonManager
import de.miraculixx.ghg_bot.utils.manager.DropDownManager
import de.miraculixx.ghg_bot.utils.manager.ModalManager
import de.miraculixx.ghg_bot.utils.manager.SlashCommandManager
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import moe.kyokobot.libdave.NativeDaveFactory
import moe.kyokobot.libdave.jda.LDJDADaveSessionFactory
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.audio.AudioModuleConfig
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
        val ktorClient = HttpClient(CIO)
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

                        HelpCommand.save()

                        JDA.shardManager?.setStatus(OnlineStatus.OFFLINE)
                        JDA.shutdown()
                    }

                    "save" -> {
                        ConfigManager.save()
                        Warnings.save()

                        HelpCommand.save()

                        println("Configs saved!")
                    }

                    "load" -> {
                        SupportData.buildRegex()
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
            setAudioModuleConfig(
                AudioModuleConfig().withDaveSessionFactory(
                    LDJDADaveSessionFactory(NativeDaveFactory())
                )
            )
            enableCache(CacheFlag.VOICE_STATE)
            setActivity(Activity.watching("sucht nach \uD83C\uDF70"))
            setStatus(OnlineStatus.IDLE)
            intents += listOf(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
            intents -= listOf(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING)
            setMemberCachePolicy(MemberCachePolicy.ALL)
        }
        JDA.awaitReady()

        SlashCommandManager.startListen(JDA)
        ButtonManager.startListen(JDA)
        DropDownManager.startListen(JDA)
        ModalManager.startListen(JDA)
        TabCompleteEvent()
        Warnings
        ThreadChannel
        AlwaysOneFree

        AutoSupportMessages()
        AutoSupportReactions()
        TicketMessages()
        SpamCheck()

        FanartHighlighting

        LOGGER.info("GHG Bot is now online!")

        command()
    }
}

val JDA by lazy { Main.JDA }
