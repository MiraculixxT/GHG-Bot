package de.miraculixx.ghg_bot.modules.user_moderation

import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.utils.cache.guildGHG
import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import de.miraculixx.ghg_bot.utils.extensions.toMember
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.reply_
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonsVoteAdmin: ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val data = it.button.id?.split(":") ?: return

        when (data[1]) {
            "FALSE" -> {
                val messageContent = it.message.contentRaw.substring(2..it.message.contentRaw.length - 3)
                val reportData = try {
                    Json.decodeFromString<ReportVoteData>(messageContent)
                } catch (e: Exception) {
                    it.reply_("```diff\n- Fehler beim parsen der Nachricht!\nError: ${e.message}```", ephemeral = true).queue()
                    return
                }
                val victim = reportData.victim.toMember()
                val addition = if (victim == null) "\n- Die Person, welche bestraft wurde ist nicht mehr auf dem Discord" else ""
                it.editMessage_("```diff\n+ Das voting wurde als falsch entschieden von @${it.user.name}!$addition```", embeds = it.message.embeds).queue()

                if (reportData.guilty) {
                    // Remove granted points and subtract false punishing
                    reportData.voteYes.map { id -> UserModerationManager.userTrust[id] }.forEach { trust -> trust?.removePoints(22) }
                    UserModerationManager.userTrust[reportData.snitch]?.removePoints(5)
                    victim?.removeTimeout()?.queue()
                } else {
                    // Remove granted points
                    reportData.voteNo.map { id -> UserModerationManager.userTrust[id] }.forEach { trust -> trust?.removePoints(7) }
                    UserModerationManager.userTrust[reportData.snitch]?.addPoints(5)
                    // Grant removed points
                    reportData.voteYes.map { id -> UserModerationManager.userTrust[id] }.forEach { trust -> trust?.addPoints(15) }
                    if (victim != null) Warnings.warnMember(victim, "Nachricht mit Inhalt welcher gegen die Regeln ist (Admin Entscheidung)", true)
                }
                return
            }

            "OPTIN" -> {
                val roles = it.member?.roles ?: return
                if (roles.contains(UserModerationManager.voteRole)) {
                    it.reply_("```diff\n- Du nimmst bereits die Teil!```", ephemeral = true).queue()
                    return
                }
                guildGHG.addRoleToMember(UserSnowflake.fromId(it.user.id), UserModerationManager.voteRole).queue()
                it.reply_("```diff\n+ Du nimmst nun am Voting teil!```", ephemeral = true).queue()
            }

            "POINTS" -> {
                val reputation = UserModerationManager.userTrust[it.user.idLong] ?: UserTrust(it.user.idLong, 0, 0)
                val percentage = reputation.getLevelPercentage()
                val progressBar = ":blue_square:".repeat(percentage / 10) + ":white_large_square:".repeat(10 - percentage / 10)

                it.reply_(embeds = listOf(Embed {
                    title = "\uD83D\uDCCA  || Deine Reputation"
                    description = "Die Reputation entscheidet wie viel Kraft deine Stimme beim voten hat. Wenn du häufig richtig votest steigt deine Reputation!\n" +
                            "‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\n" +
                            "<:blanc:1193179205589008455> <:blanc:1193179205589008455> <:blanc:1193179205589008455><:blanc:1193179205589008455> :star: **LEVEL ${reputation.level}**\n" +
                            "**${percentage.twoDigitCheck()}%** $progressBar ${reputation.points}/${reputation.getCurrentLevelMax()}"
                    color = 0x27a7e9
                }), ephemeral = true).queue()
            }
            else -> return
        }
    }

    private fun Int.twoDigitCheck(): String {
        return if (this < 10) "0$this" else toString()
    }
}