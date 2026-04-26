package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.utils.cache.guildGHG
import de.miraculixx.ghg_bot.utils.entities.EventListener
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent
import net.dv8tion.jda.api.components.section.SectionAccessoryComponentUnion
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.internal.entities.ForumTagImpl
import kotlin.collections.plus

object FanartHighlighting: EventListener {
    private val fanartChannel = 1048240752095936542
    private val highlightTag = ForumTagImpl(1048241864244990013)
    private val fanartTag = ForumTagImpl(1048241662280876133)
    private var roleID = 1451241076697469080

    override val listener: CoroutineEventListener = JDA.listener<MessageReactionAddEvent> {
        CoroutineScope(Dispatchers.Default).launch {
            val thread = it.channel as? ThreadChannel ?: return@launch
            if (thread.parentChannel.idLong != fanartChannel) return@launch

            // Check if original message
            val startMessage = thread.retrieveStartMessage().await()
            if (it.messageIdLong != startMessage.idLong) return@launch

            val count = it.retrieveMessage().await().reactions.maxOf { msg -> msg.count }
            val member = thread.owner ?: guildGHG.retrieveMemberById(thread.ownerIdLong).await()

            // Add highlight tag if reactions are 25 or more
            if (count >= 25) addTag(member, startMessage, thread)

            // Add fanart role if reactions are 35 or more
            if (count >= 40) addRole(member, startMessage)
        }
    }

    private suspend fun addTag(member: Member?, message: Message, thread: ThreadChannel) {
        println("Detected 25+ (${thread.id})")
        val tags = thread.appliedTags
        if (tags.contains(highlightTag) || !tags.contains(fanartTag)) return
        println(" - Adding highlight tag")
        thread.manager.setAppliedTags(thread.appliedTags + highlightTag).queue()
        member?.user?.sendHighlightAnnouncement(false, message.jumpUrl)
    }

    private suspend fun addRole(member: Member?, message: Message) {
        member ?: return
        println("Detected 40+ (Member: ${member.idLong})")
        val role = guildGHG.getRoleById(roleID)
        if (role == null || member.roles.contains(role)) return
        println(" - Adding fanart role")
        member.guild.addRoleToMember(member, role).queue()
        member.user.sendHighlightAnnouncement(true, message.jumpUrl)
    }


    suspend fun User.sendHighlightAnnouncement(isRole: Boolean, link: String) {
        val amount = if (isRole) "40" else "25"
        val award = if (isRole) "du somit die exklusive **Hyped Fanart Role** mit Highlighting freigeschaltet" else "somit den **Hyped Tag**"
        val msg = Container.of(
            Section.of(
                Button.link(link, "Zum Post"),
                TextDisplay.of("## <:hype:1451269147991212235> Hyped Post")
            ),
            Separator.createInvisible(Separator.Spacing.SMALL),
            TextDisplay.of("Dein Fanart hat **$amount** Reaktionen und $award!\n" +
                    "Vielen Dank für deinen tollen Beitrag zur Community <:zickzackLove:1451279792979579032>")
        )

        try {
            val dm = openPrivateChannel().await()
            dm.sendMessageComponents(msg).queue()
        } catch (_: Exception) {}
    }
}