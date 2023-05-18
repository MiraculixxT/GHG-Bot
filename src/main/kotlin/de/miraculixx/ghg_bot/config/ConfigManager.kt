package de.miraculixx.ghg_bot.config

import de.miraculixx.ghg_bot.modules.auto_support.SupportFilter
import de.miraculixx.ghg_bot.utils.cache.supportRegex
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ConfigManager {
    val credentials: Credentials
    val regex: MutableMap<SupportFilter, MutableList<String>>

    fun updateRegex() {
        regex.forEach { (filter, entries) ->
            val finalFilter = buildString {
                entries.forEach { e ->
                    append("$e|")
                }
            }
            supportRegex[filter] = Regex(finalFilter.removeSuffix("|"), RegexOption.IGNORE_CASE)
        }
        save()
    }

    fun save() {
        val configFolder = File("config")
        File("${configFolder.path}/regex.json").writeText(Json.encodeToString(regex))
    }

    init {
        val configFolder = File("config")
        if (!configFolder.exists() || !configFolder.isDirectory) configFolder.mkdirs()

        val credentialFile = File("${configFolder.path}/core.json")
        if (!credentialFile.exists()) {
            credentialFile.createNewFile()
            credentialFile.writeText("{}")
        }
        credentials = Json.decodeFromString(credentialFile.readText())

        val regexFile = File("${configFolder.path}/regex.json")
        if (!regexFile.exists()) {
            regexFile.createNewFile()
            regexFile.writeText("{}")
        }
        regex = Json.decodeFromString(regexFile.readText())

        updateRegex()
    }

    @Serializable
    data class Credentials(val dcToken: String = "")
}