package de.miraculixx.ghg_bot.utils.entities

import dev.minn.jda.ktx.events.CoroutineEventListener


interface EventListener {
    val listener: CoroutineEventListener
    fun stopListen() {
        listener.cancel()
    }
}