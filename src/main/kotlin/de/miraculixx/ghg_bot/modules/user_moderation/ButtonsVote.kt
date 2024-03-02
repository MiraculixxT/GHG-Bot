package de.miraculixx.ghg_bot.modules.user_moderation

import de.miraculixx.ghg_bot.modules.moderation.Warnings
import de.miraculixx.ghg_bot.utils.cache.teamRole
import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import de.miraculixx.ghg_bot.utils.extensions.toMember
import de.miraculixx.ghg_bot.utils.extensions.toUUID
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonsVote : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val data = it.button.id?.split(":") ?: return
        val reportID = data.getOrNull(2)?.toUUID() ?: return
        val report = UserModerationManager.cases[reportID] ?: return

        if (report.message.author.id == it.user.id) {
            it.reply_("```diff\n- Du kannst nicht für deine eigene Nachricht abstimmen!```", ephemeral = true).queue()
            return
        }

        val userTrust = UserModerationManager.userTrust.getOrPut(it.user.idLong) { UserTrust(it.user.idLong, 0, 0) }
        val isMod = it.member?.roles?.contains(teamRole) ?: false

        if (report.snitch.id == it.user.id && !isMod) {
            it.reply_("```diff\n- Du kannst nicht für deine eigene Meldung abstimmen!```", ephemeral = true).queue()
            return
        }

        when (data[1]) {
            "VOTE-YES" -> {
                if (report.voteYes.add(userTrust)) {
                    it.deferEdit().queue()
                    report.voteNo.remove(userTrust)
                } else {
                    it.reply("```diff\n- Du hast bereits für verdächtig abgestimmt!```").setEphemeral(true).queue()
                    return
                }
            }

            "VOTE-NO" -> {
                if (report.voteNo.add(userTrust)) {
                    it.deferEdit().queue()
                    report.voteYes.remove(userTrust)
                } else {
                    it.reply("```diff\n- Du hast bereits für unschuldig abgestimmt!```").setEphemeral(true).queue()
                    return
                }
            }

            else -> return
        }

        val votes = report.calculateVotes()
        val allVotes = votes.first + votes.second
        if (allVotes >= 10 || isMod) {
            val percent = votes.first / allVotes
            println("Voter Percentage: $percent (${votes.first} / ${votes.second})")

            if (percent >= 0.7) { // Warn reported user and delete
                val member = report.message.author.idLong.toMember()
                if (member != null) Warnings.warnMember(member, "Nachricht ist nicht Regelkonform (Community Voting)\n```fix\n${report.message.contentDisplay}```", true)
                else println("Report target was null!")
                report.message.delete().queue()

                val reporterTrust = UserModerationManager.userTrust.getOrPut(report.snitch.idLong) { UserTrust(report.snitch.idLong, 0, 0) }
                reporterTrust.addPoints(3)
                report.voteYes.forEach { trust -> trust.addPoints(7) }
                kotlin.runCatching {
                    report.snitch.user.openPrivateChannel().complete().sendMessageEmbeds(Embed {
                        title = "<:yes:1176153745684439081> || Report Bestätigt"
                        description = "Deine Meldung wurde von der Community bestätigt!\nVielen Dank für deine Meldung!"
                        color = 0x24ba13
                        footer("User Report System Beta - Please report bugs to @miraculixx")
                    }).queue()
                }
                report.cleanUp(true, votes)

            } else if (percent <= 0.4) { // Delete report and untrust yes voters
                val reporterTrust = UserModerationManager.userTrust.getOrPut(report.snitch.idLong) { UserTrust(report.snitch.idLong, 0, 0) }
                reporterTrust.removePoints(5)
                report.voteYes.forEach { trust -> trust.removePoints(15) }
                report.voteNo.forEach { trust -> trust.addPoints(7) }
                kotlin.runCatching {
                    report.snitch.user.openPrivateChannel().complete().sendMessageEmbeds(Embed {
                        title = "<:no:1176153726747152495> || Report Abgelehnt"
                        description = "Deine Meldung wurde von der Community abgelehnt!\nBitte achte darauf nur Nachrichten zu melden, welche gegen die <#484677070522417162> sind!"
                        color = 0xa31c1c
                        footer("User Report System Beta - Please report bugs to @miraculixx")
                    }).queue()
                }
                report.cleanUp(false, votes)
            }
        } else {
            report.update(it.hook, votes)
        }
    }
}