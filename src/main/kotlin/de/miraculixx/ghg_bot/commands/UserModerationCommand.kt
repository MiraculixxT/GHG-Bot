package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.user_moderation.UserModerationManager
import de.miraculixx.ghg_bot.modules.user_moderation.UserTrust
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class UserModerationCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        it.guild ?: return
        val userID = it.getOption("user")?.asUser?.idLong ?: return

        when (it.subcommandName) {
            "add" -> {
                val points = it.getOption("points")?.asInt ?: return
                val rep = UserModerationManager.userTrust.getOrPut(userID) { UserTrust(userID, 0, 0) }
                rep.addPoints(points)
                it.reply_("Der User hat nun ${rep.points} Punkte mit dem Level ${rep.level}!", ephemeral = true).queue()
            }

            "remove" -> {
                val points = it.getOption("points")?.asInt ?: return
                val rep = UserModerationManager.userTrust.getOrPut(userID) { UserTrust(userID, 0, 0) }
                rep.removePoints(points)
                it.reply_("Der User hat nun ${rep.points} Punkte mit dem Level ${rep.level}!", ephemeral = true).queue()
            }

            "get" -> {
                val rep = UserModerationManager.userTrust.getOrPut(userID) { UserTrust(userID, 0, 0) }
                it.reply_("Der User hat ${rep.points} Punkte mit dem Level ${rep.level}!", ephemeral = true).queue()
            }
        }
    }
}