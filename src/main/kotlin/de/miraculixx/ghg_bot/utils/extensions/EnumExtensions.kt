package de.miraculixx.ghg_bot.utils.extensions

import java.util.UUID

inline fun <reified T : Enum<T>> enumOf(type: String?): T? {
    if (type == null) return null
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun String.toUUID(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}