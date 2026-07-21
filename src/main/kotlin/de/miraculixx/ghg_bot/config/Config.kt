@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package de.miraculixx.ghg_bot.config

import de.miraculixx.ghg_bot.utils.extensions.enumOf
import de.miraculixx.ghg_bot.utils.log.LOGGER
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import kotlin.io.path.Path

class Config(stream: InputStream?, private val name: String) {
    private val yaml: Yaml = Yaml()
    private val configMap: MutableMap<String, Any>

    fun set(key: String, value: Any) {
        configMap[key] = value
    }

    fun getString(name: String): String {
        return configMap[name].toString()
    }

    fun getStringList(name: String): List<String> {
        return try {
            configMap[name] as List<String>
        } catch (e: ClassCastException) {
            LOGGER.error("Value $name in Config ${this.name} cannot be casted to List<String>")
            emptyList()
        } catch (e: NullPointerException) {
            LOGGER.error("Value $name in Config ${this.name} is empty")
            emptyList()
        }
    }

    fun <T> getObjectList(name: String): LinkedHashMap<String, T> {
        return try {
            val origin = configMap[name] as List<Map<String, T>>
            val map = LinkedHashMap<String, T>()
            origin.forEach {
                val data = it.entries.first()
                map[data.key] = data.value
            }
            map
        } catch (e: ClassCastException) {
            LOGGER.error("Value $name in Config ${this.name} cannot be casted to List<Map<String, T>>")
            linkedMapOf()
        } catch (e: NullPointerException) {
            LOGGER.error("Value $name in Config ${this.name} is empty")
            linkedMapOf()
        }
    }

    fun getInt(name: String): Int {
        return getString(name).toIntOrNull() ?: 0
    }

    fun getLong(name: String): Long {
        return getString(name).toLongOrNull() ?: 0
    }

    fun getBoolean(name: String): Boolean {
        return getString(name).lowercase() == "true"
    }

    inline fun <reified T : Enum<T>> getEnum(name: String): T? {
        return enumOf<T>(getString(name))
    }


    private fun loadConfig(file: File, input: InputStream) {
        LOGGER.info("Creating new config file: $name.yml")
        if (!file.exists()) {
            file.createNewFile()
            file.writeBytes(input.readAllBytes())
        }
    }

    init {
        LOGGER.info("Loading config: $name.yml")
        val file = Path("config/$name.yml").toFile()
        configMap = if (stream != null) {
            if (!file.exists()) loadConfig(file, stream)

            try {
                yaml.load<Map<String, Any>>(file.inputStream()).toMutableMap()
            } catch (e: Exception) {
                e.printStackTrace()
                LOGGER.error("Failed to load Configuration File. ^^ Reason above ^^")
                LOGGER.error("Config Path -> ${file.path}")
                mutableMapOf()
            }
        } else {
            LOGGER.error("Configuration file is null")
            mutableMapOf()
        }
    }
}