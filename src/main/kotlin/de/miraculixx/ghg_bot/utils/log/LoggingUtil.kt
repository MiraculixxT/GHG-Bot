@file:Suppress("unused")

package de.miraculixx.ghg_bot.utils.log

import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.time.Instant

var consoleChannel: MessageChannel? = null

fun String.log(color: Color = Color.WHITE) {
    printToConsole("\u001B[${Color.BLUE.code}mINFO: \u001B[${color.code}m$this", "**INFO:** $this")
}

fun String.error() {
    printToConsole("\u001B[${Color.BLUE.code}mERROR: \u001B[${Color.RED.code}m$this", "**ERROR:** $this")
}

private fun printToConsole(input: String, raw: String) = getDefaultScope().launch {
    println("$input\u001B[0m")

    //DC Logging
    val timestamp = Instant.now().epochSecond
    consoleChannel?.send("<t:$timestamp:T> $raw")?.queue()
}

private fun prettyNumber(int: Int): String {
    return if (int <= 9) "0$int" else int.toString()
}

enum class Color(val code: Byte) {
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    GRAY(90),
    WHITE(97)
}