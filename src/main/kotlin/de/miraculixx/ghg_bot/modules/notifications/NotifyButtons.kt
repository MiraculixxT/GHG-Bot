package de.miraculixx.ghg_bot.modules.notifications

import de.miraculixx.ghg_bot.utils.cache.guildGHG
import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class NotifyButtons : ButtonEvent {
    private val ytMainRole = guildGHG.getRoleById(604279182872018974)
    private val ytSecondRole = guildGHG.getRoleById(604279184038297620)
    private val twitchRole = guildGHG.getRoleById(604277191823589396)
    private val bellActive = Emoji.fromFormatted("\uD83D\uDD14")
    private val bellDisabled = Emoji.fromFormatted("\uD83D\uDD15")
    private val notifyEmbed = Embed {
        description = "Klicke auf den jeweiligen Knopf unter dieser Nachricht, um Benachrichtigungen zu aktivieren oder zu deaktivieren!\n" +
                "\n" +
                "> :red_square: -> Benachrichtigung ist **deaktiviert**\n" +
                "> :green_square: -> Benachrichtigung ist **aktiviert**"
        color = 0xb800ff
    }

    override suspend fun trigger(it: ButtonInteractionEvent) {
        if (ytMainRole == null || ytSecondRole == null || twitchRole == null) {
            it.reply_("Ein Fehler ist aufgetreten!").queue()
            return
        }
        val member = it.member ?: return

        when (it.componentId) {
            "NOTIFY" -> it.reply_(embeds = listOf(notifyEmbed), components = listOf(member.getNotifyButtons()), ephemeral = true).queue()

            "NOTIFY-YTMAIN" -> it.editComponents(member.getNotifyButtons(main = member.toggleRole(ytMainRole))).queue()

            "NOTIFY-YTSECOND" -> it.editComponents(member.getNotifyButtons(second = member.toggleRole(ytSecondRole))).queue()

            "NOTIFY-TWITCH" -> it.editComponents(member.getNotifyButtons(twitch = member.toggleRole(twitchRole))).queue()
        }
    }

    private fun Member.getNotifyButtons(main: Boolean? = null, second: Boolean? = null, twitch: Boolean? = null): ActionRow {
        val roles = roles

        val ytMainButton = getButton(main ?: roles.contains(ytMainRole), "NOTIFY-YTMAIN", "YT Hauptkanal")
        val ytSecondButton = getButton(second ?: roles.contains(ytSecondRole), "NOTIFY-YTSECOND", "YT Zweitkanal")
        val twitchButton = getButton(twitch ?: roles.contains(twitchRole), "NOTIFY-TWITCH", "Twitch")

        return ActionRow.of(ytMainButton, ytSecondButton, twitchButton)
    }

    private fun getButton(active: Boolean, id: String, label: String): Button {
        return button(id, label, if (active) bellActive else bellDisabled, if (active) ButtonStyle.SUCCESS else ButtonStyle.DANGER)
    }

    /**
     * @return true if role is now enabled
     */
    private fun Member.toggleRole(role: Role): Boolean {
        return if (roles.contains(role)) {
            guildGHG.removeRoleFromMember(this, role).queue()
            false
        } else {
            guildGHG.addRoleToMember(this, role).queue()
            true
        }
    }
}