package de.miraculixx.ghg_bot.modules.moderation

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.send
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Member
import java.io.File
import java.time.Duration
import kotlin.time.toKotlinDuration

object Warnings {
    private val file = File("config/warns.json")
    private var warnings: MutableMap<String, Int> = mutableMapOf()

    fun setWarns(id: String, amount: Int) {
        warnings[id] = amount
    }

    fun getWarnings(id: String): Int = warnings[id] ?: 0

    fun warnMember(member: Member, reason: String): Duration {
        val id = member.id
        val newWarnings = warnings[id]?.plus(1) ?: 1
        warnings[id] = newWarnings
        val duration = when (newWarnings) {
            1 -> Duration.ofMinutes(1)
            2 -> Duration.ofMinutes(10)
            3 -> Duration.ofHours(1)
            4 -> Duration.ofHours(6)
            5 -> Duration.ofHours(12)
            6 -> Duration.ofDays(1)
            7 -> Duration.ofDays(2)
            8 -> Duration.ofDays(7)
            else -> Duration.ofDays(14)
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

    fun save() {
        if (!file.exists()) file.parentFile.mkdirs()
        file.writeText(Json.encodeToString(warnings))
    }

    init {
        if (!file.exists()) file.parentFile.mkdirs()
        else {
            warnings = Json.decodeFromString(file.readText().ifBlank { "{}" })
        }
    }
}