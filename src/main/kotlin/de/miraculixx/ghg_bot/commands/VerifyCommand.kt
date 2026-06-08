package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.moderation.SpamCheck
import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import kotlin.random.Random

object VerifyCommand : SlashCommandEvent, ButtonEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val member = it.member ?: return
        if (member.roles.any { role -> role.idLong == SpamCheck.VERIFIED_LINKS_ROLE }) {
            it.reply("Du bist bereits verifiziert. Du willst trotzdem bisschen Quick-Math machen? Dann ab in <#1036242559959302164> und `/quick-math`!").setEphemeral(true).queue()
            return
        }

        val left = Random.nextInt(-30, 30)
        val right = Random.nextInt(-30, 30)
        val addition = Random.nextBoolean()
        val answer = if (addition) left + right else left - right
        val choices = buildChoices(answer)

        it.reply("Captcha: Was ist `$left ${if (addition) "+" else "-"} $right`?")
            .addComponents(ActionRow.of(choices.map { choice -> Button.secondary("VERIFY:${member.idLong}:$answer:$choice", choice.toString()) }))
            .setEphemeral(true)
            .queue()
    }

    override suspend fun trigger(it: ButtonInteractionEvent) {
        val member = it.member ?: return
        val parts = it.componentId.split(":")
        val targetUserId = parts.getOrNull(1)?.toLongOrNull()
        val answer = parts.getOrNull(2)?.toIntOrNull()
        val selected = parts.getOrNull(3)?.toIntOrNull()

        if (parts.size != 4 || targetUserId == null || answer == null || selected == null) {
            it.reply("Dieses Captcha ist ungültig. Bitte nutze `/verify` erneut.").setEphemeral(true).queue()
            return
        }

        if (targetUserId != it.user.idLong) {
            it.reply("Dieses Captcha gehört nicht dir.").setEphemeral(true).queue()
            return
        }

        if (selected != answer) {
            it.editMessage("Captcha falsch. Bitte nutze `/verify` erneut.")
                .setComponents(emptyList())
                .queue()
            return
        }

        val role = it.guild?.getRoleById(SpamCheck.VERIFIED_LINKS_ROLE)
        if (role == null) {
            it.editMessage("Die Verify-Rolle wurde nicht gefunden. Bitte melde dich beim Team.")
                .setComponents(emptyList())
                .queue()
            return
        }

        it.guild?.addRoleToMember(member, role)?.queue()
        it.editMessage("Verifizierung erfolgreich! Deine Nachrichten werden jetzt nicht mehr geflaggt bis zum nächsten Timeout/Warn")
            .setComponents(emptyList())
            .queue()
    }

    private fun buildChoices(answer: Int): List<Int> {
        val choices = mutableSetOf(answer)
        while (choices.size < 3) {
            choices += answer + Random.nextInt(-10, 10)
        }
        return choices.shuffled()
    }
}
