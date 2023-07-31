package de.miraculixx.ghg_bot.modules.moderation

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.cache.modLog
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.*

class SpamCheck : EventListener {
    private val imageSpam: MutableMap<String, Int> = mutableMapOf()
    private val sameMessageSpam: MutableMap<String, String> = mutableMapOf()

    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        val member = it.member ?: return@listener
        val id = member.id
        if (member.user.isBot) return@listener
        if (member.hasPermission(Permission.KICK_MEMBERS)) return@listener

        if (it.checkAttachmentSpam(id, member)) return@listener
        if (it.checkAds(member)) return@listener
        if (it.checkSimilarity(id, member)) return@listener
    }


    private fun MessageReceivedEvent.checkAds(member: Member): Boolean {
        if (channel.id != "1088197598780866711") return false
        val msg = message.contentRaw
        return if (msg.contains("discord", true) || msg.contains(" ip", true)) {
            Warnings.warnMember(member, "```fix\nBitte sende keine Werbung für deine oder andere Projekte!```")
            message.delete().queue()
            modLog.send("```fix\nWerbung in spieler-suche Channel:\n$msg``` ${member.asMention} (${member.id})").queue()
            true
        } else false
    }

    private fun MessageReceivedEvent.checkAttachmentSpam(id: String, member: Member): Boolean {
        val attachment = message.attachments
        val count = if (attachment.isEmpty()) {
            imageSpam.remove(id)
            return false
        } else imageSpam[id]?.plus(1) ?: 1
        imageSpam[id] = count
        return if (count >= 3) {
            Warnings.warnMember(member, "Bitte spamme keine Gifs, Bilder, Videos, etc...!")
            message.delete().queue()
            modLog.send("```fix\nAttachment Spam ($count) in aufeinanderfolgenden Nachrichten.``` ${member.asMention} (${member.id})").queue()
            true
        } else false
    }

    private fun MessageReceivedEvent.checkSimilarity(id: String, member: Member): Boolean {
        val unformatted = message.contentStripped
        val last = sameMessageSpam[id]
        sameMessageSpam[id] = unformatted
        if (last == null || last.length < 8) return false
        val sim = similarity(last, unformatted)
        return if (sim >= 0.8) {
            Warnings.warnMember(member, "Du wiederholst dich! Bitte schreibe dieselbe Nachricht nicht mehrmals hintereinander.\n```fix\nNachricht: ${message.contentRaw}```")
            message.delete().queue()
            modLog.send("```fix\nGleiche Nachrichten Spam. ${(sim * 100).toInt()}% gleicher Inhalt in aufeinanderfolgenden Nachrichten.\n\n1. $last\n\n2. $unformatted``` ${member.asMention} (${member.id})").queue()
            true
        } else false
    }

    /**
     * @return 0 -> 0% 1 -> same message
     */
    private fun similarity(s1: String, s2: String): Double {
        var longer = s1
        var shorter = s2
        if (s1.length < s2.length) {
            longer = s2
            shorter = s1
        }
        val longerLength = longer.length
        return if (longerLength == 0) {
            1.0 // both strings are empty?
        } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://r...content-available-to-author-only...e.org/wiki/Levenshtein_distance#Java
    private fun editDistance(st1: String, st2: String): Int {
        var s1 = st1
        var s2 = st2
        s1 = s1.lowercase(Locale.getDefault())
        s2 = s2.lowercase(Locale.getDefault())
        val costs = IntArray(s2.length + 1)
        for (i in 0..s1.length) {
            var lastValue = i
            for (j in 0..s2.length) {
                if (i == 0) costs[j] = j else {
                    if (j > 0) {
                        var newValue = costs[j - 1]
                        if (s1[i - 1] != s2[j - 1]) newValue = newValue.coerceAtMost(lastValue).coerceAtMost(costs[j]) + 1
                        costs[j - 1] = lastValue
                        lastValue = newValue
                    }
                }
            }
            if (i > 0) costs[s2.length] = lastValue
        }
        return costs[s2.length]
    }
}