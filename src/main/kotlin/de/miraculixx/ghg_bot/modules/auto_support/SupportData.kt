package de.miraculixx.ghg_bot.modules.auto_support

import de.miraculixx.ghg_bot.config.ConfigManager.autoSupport
import kotlin.collections.set

object SupportData {
    val supportRegex: MutableMap<SupportFilter, Regex> = mutableMapOf()

    fun buildRegex() {
        autoSupport.regexMap.forEach { (filter, entries) ->
            val finalFilter = buildString {
                entries.forEach { e ->
                    append("$e|")
                }
            }
            supportRegex[filter] = Regex(finalFilter.removeSuffix("|"), RegexOption.IGNORE_CASE)
        }
    }
}