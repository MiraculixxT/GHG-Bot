package de.miraculixx.ghg_bot.utils.extensions

import de.miraculixx.ghg_bot.JDA
import net.dv8tion.jda.api.entities.Message

fun Message.mentionlessContent(): String {
    var rawMessage = contentRaw
    var mentionStatus = 0
    var mentionID = ""
    rawMessage.forEach { char ->
        when (mentionStatus) {
            0 -> if (char == '<') mentionStatus = 1
            1 -> if (char == '@') mentionStatus = 2
            2 -> {
                when {
                    char == '&' -> mentionStatus = 3
                    char == '>' -> {
                        rawMessage = rawMessage.replace("<@$mentionID>", "``@${JDA.getSaveUser(mentionID)?.name ?: "Unbekannt"}``")
                        mentionStatus = 0
                        mentionID = ""
                    }

                    char.isDigit() -> mentionID += char
                    else -> {
                        mentionStatus = 0
                        mentionID = ""
                    }
                }
            }

            3 -> {
                if (char.isDigit()) {
                    mentionID += char
                    return@forEach
                }
                if (char == '>') {
                    rawMessage = rawMessage.replace("<@&$mentionID>", "``@${JDA.getRoleById(mentionID)?.name ?: "Unbekannt"}``")
                }
                mentionStatus = 0
                mentionID = ""
            }
        }
    }
    return rawMessage
}