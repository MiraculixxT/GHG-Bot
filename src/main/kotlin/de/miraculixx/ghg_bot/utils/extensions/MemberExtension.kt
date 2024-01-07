package de.miraculixx.ghg_bot.utils.extensions

import de.miraculixx.ghg_bot.utils.cache.guildGHG
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

fun JDA.getSaveUser(id: String): User? {
    return try {
        getUserById(id) ?: retrieveUserById(id).complete()
    } catch (_: Exception) {
        null
    }
}

fun JDA.getSaveUser(id: Long) = getSaveUser(id.toString())

fun Long.toMember(): Member? =
    guildGHG.getMemberById(this) ?: try {
        guildGHG.retrieveMemberById(this).complete()
    } catch (_: Exception) { null }