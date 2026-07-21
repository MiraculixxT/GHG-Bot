package de.miraculixx.ghg_bot.modules.other

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.config.ConfigManager
import de.miraculixx.ghg_bot.utils.cache.guildGHG
import de.miraculixx.ghg_bot.utils.entities.EventListener
import de.miraculixx.ghg_bot.utils.log.LOGGER
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.internal.entities.ForumTagImpl

object FanartHighlighting: EventListener {
    private val highlightTag = ForumTagImpl(ConfigManager.specialChannels.fanart.highlightTag)
    private val fanartTag = ForumTagImpl(ConfigManager.specialChannels.fanart.fanartTag)
    private var roleID = ConfigManager.specialChannels.fanart.awardRole
    private var channelID = ConfigManager.specialChannels.fanart.channel

    override val listener: CoroutineEventListener = JDA.listener<MessageReactionAddEvent> {
        CoroutineScope(Dispatchers.Default).launch {
            val thread = it.channel as? ThreadChannel ?: return@launch
            if (thread.parentChannel.idLong != channelID) return@launch

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
        LOGGER.info("Fanart thread ${thread.id} reached 25+ reactions")
        val tags = thread.appliedTags
        if (tags.contains(highlightTag) || !tags.contains(fanartTag)) return
        LOGGER.info("Adding highlight tag to fanart thread ${thread.id}")
        thread.manager.setAppliedTags(thread.appliedTags + highlightTag).queue()
        member?.user?.sendHighlightAnnouncement(false, message.jumpUrl)
    }

    private suspend fun addRole(member: Member?, message: Message) {
        member ?: return
        LOGGER.info("Fanart member ${member.idLong} reached 40+ reactions")
        val role = guildGHG.getRoleById(roleID)
        if (role == null || member.roles.contains(role)) return
        LOGGER.info("Adding fanart role to member ${member.idLong}")
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