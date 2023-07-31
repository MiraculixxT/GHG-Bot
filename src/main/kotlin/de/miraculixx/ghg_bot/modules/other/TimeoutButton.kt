package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import java.time.Duration

class TimeoutButton : ButtonEvent {

    override suspend fun trigger(it: ButtonInteractionEvent) {
        val member = it.member ?: return

        when (it.componentId) {
            "TIMEOUT:1" -> {
                member.timeoutFor(Duration.ofHours(1)).queue()
                it.reply_("Hier ist dein kostenloser 1 Stunde Timeout :gift:", ephemeral = true).queue()
            }

            "TIMEOUT:2" -> {
                member.timeoutFor(Duration.ofDays(1)).queue()
                it.reply_("Hier ist dein kostenloser 1 Stunde Timeout :gift:", ephemeral = true).queue()
            }
        }
    }
}