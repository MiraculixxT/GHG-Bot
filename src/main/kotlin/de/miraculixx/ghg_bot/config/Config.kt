@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package de.miraculixx.ghg_bot.config

import de.miraculixx.ghg_bot.utils.extensions.enumOf
import de.miraculixx.ghg_bot.utils.log.log
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
            "ERROR > Value $name in Config ${this.name} cannot be casted to List<String>".log()
            emptyList()
        } catch (e: NullPointerException) {
            "ERROR > Value $name in Config ${this.name} is empty".log()
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
            "ERROR > Value $name in Config ${this.name} cannot be casted to List<Map<String, T>>".log()
            linkedMapOf()
        } catch (e: NullPointerException) {
            "ERROR > Value $name in Config ${this.name} is empty".log()
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
        ">> Create new Config File - $name".log()
        if (!file.exists()) {
            file.createNewFile()
            file.writeBytes(input.readAllBytes())
        }
    }

    init {
        ">> Load Config - $name".log()
        val file = Path("config/$name.yml").toFile()
        configMap = if (stream != null) {
            if (!file.exists()) loadConfig(file, stream)

            try {
                yaml.load<Map<String, Any>>(file.inputStream()).toMutableMap()
            } catch (e: Exception) {
                e.printStackTrace()
                "ERROR - Failed to load Configuration File. ^^ Reason above ^^".log()
                "ERROR - Config Path -> ${file.path}".log()
                mutableMapOf()
            }
        } else {
            "ERROR - Configuration file is null".log()
            mutableMapOf()
        }
    }
}