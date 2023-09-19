package de.miraculixx.ghg_bot.modules.moderation

import de.miraculixx.ghg_bot.utils.cache.modLog
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Member
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toKotlinDuration

object Warnings {
    private val file = File("config/warns.json")
    private var manualWarnings: MutableMap<String, Int> = mutableMapOf()
    private var autoWarnings: MutableMap<String, Int> = mutableMapOf()

    fun setWarns(id: String, amount: Int) {
        manualWarnings[id] = amount
    }

    fun getWarnings(id: String): Int = manualWarnings[id] ?: 0

    fun warnMember(member: Member, reason: String, admin: Boolean): Duration {
        val id = member.id

        val duration = if (admin) {
            val newWarnings = manualWarnings[id]?.plus(1) ?: 1
            manualWarnings[id] = newWarnings
            when (newWarnings) {
                1 -> Duration.ofMinutes(1)
                2 -> Duration.ofMinutes(10)
                3 -> Duration.ofMinutes(30)
                4 -> Duration.ofHours(1)
                5 -> Duration.ofHours(2)
                6 -> Duration.ofHours(6)
                7 -> Duration.ofHours(12)
                8 -> Duration.ofDays(1)
                9 -> Duration.ofDays(2)
                10 -> Duration.ofDays(3)
                11 -> Duration.ofDays(7)
                else -> Duration.ofDays(14)
            }
        } else {
            val newWarnings = autoWarnings[id]?.plus(1) ?: 1
            autoWarnings[id] = newWarnings
            when (newWarnings) {
                1 -> Duration.ofMinutes(1)
                2 -> Duration.ofMinutes(15)
                3 -> Duration.ofMinutes(30)
                4 -> Duration.ofMinutes(45)
                5 -> Duration.ofHours(1)
                6 -> Duration.ofHours(5)
                7 -> Duration.ofHours(12)
                else -> Duration.ofHours(24)
            }
        }

        member.timeoutFor(duration).queue()
        member.user.openPrivateChannel().flatMap { it.send(embeds = listOf(Embed {
            title = "Warnung Erhalten"
            description = "Aufgrund eines Regelbruchs wurdest du gewarnt!\n\n$reason\n\n- Timeout Dauer: **${duration.toKotlinDuration()}**"
            footer {
                name = "Bei Fragen oder Problemen @miraculixx kontaktieren"
            }
        })) }.queue()
        return duration
    }

    private fun task() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val time = Instant.now().atZone(ZoneId.systemDefault())
                if (time.hour == 0 && time.minute in 0..1) {
                    autoWarnings.clear()
                    modLog.send("```diff\n+ Alle automatischen Timeouts wurden resettet!```").queue()
                }
                delay(1.minutes)
            }
        }
    }

    fun save() {
        if (!file.exists()) file.parentFile.mkdirs()
        file.writeText(Json.encodeToString(manualWarnings))
    }

    init {
        if (!file.exists()) file.parentFile.mkdirs()
        else {
            manualWarnings = Json.decodeFromString(file.readText().ifBlank { "{}" })
        }
        task()
    }
}