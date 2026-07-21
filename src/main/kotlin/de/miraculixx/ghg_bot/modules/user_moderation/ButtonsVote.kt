package de.miraculixx.ghg_bot.modules.user_moderation

import de.miraculixx.ghg_bot.utils.cache.teamRole
import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import de.miraculixx.ghg_bot.utils.extensions.toUUID
import dev.minn.jda.ktx.interactions.components.TextInput
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonsVote : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val data = it.button.customId?.split(":") ?: return
        val reportID = data.getOrNull(2)?.toUUID() ?: return
        val report = UserModerationManager.cases[reportID] ?: return

        val member = it.member ?: return
        if (!member.roles.contains(teamRole)) {
            it.reply_("```diff\n- Nur Moderatoren können Meldungen bearbeiten!```", ephemeral = true).queue()
            return
        }

        when (data[1]) {
            "IGNORE" -> {
                report.complete("Ignoriert", member)
                it.reply_("```diff\n+ Meldung ignoriert.```", ephemeral = true).queue()
            }

            "WARN" -> {
                it.replyModal("REPORT:WARN:$reportID", "Verwarnen") {
                    label("Grund") {
                        child = TextInput("REASON", TextInputStyle.PARAGRAPH, placeholder = "Warn Grund") {
                            required = true
                        }
                    }
                }.queue()
            }

            "BAN" -> {
                val targetId = report.message.author.id
                report.complete("Gebannt", member)
                it.reply_(UserModerationManager.sapphireBanCommand(targetId, report.reason), ephemeral = true).queue()
                it.reply_("Oberen Befehl kopieren zum bannen via Sapphire (mobile -> copy message)", ephemeral = true).queue()
            }

            else -> return
        }
    }
}
