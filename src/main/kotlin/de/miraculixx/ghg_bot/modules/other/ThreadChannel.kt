package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.cache.teamRole
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object ThreadChannel : EventListener {
    private val channels = listOf(1176151086722195497)
    private val emojiVoteUp = Emoji.fromFormatted("<:yes:1176153745684439081>")
    private val emojiVoteDown = Emoji.fromFormatted("<:no:1176153726747152495>")

    override val listener: CoroutineEventListener = JDA.listener<MessageReceivedEvent> {
        val author = it.member ?: return@listener
        val id = it.channel.idLong
        if (channels.contains(id)) {
            if (author.user.isBot || author.roles.contains(teamRole)) return@listener
            val message = it.message
            message.createThreadChannel("Diskussions Thread...").flatMap { thread ->
                thread.send("${author.asMention} hier kannst du mit anderen über deine Idee diskutieren. Bitte update den originalen Post wenn du Änderungen vornehmen möchtest!")
            }.queue()
            message.addReaction(emojiVoteUp).queue()
            message.addReaction(emojiVoteDown).queue()
        }
    }
}