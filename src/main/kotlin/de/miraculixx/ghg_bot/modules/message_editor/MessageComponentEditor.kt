package de.miraculixx.ghg_bot.modules.message_editor

import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow

class MessageComponentEditor(
    originalMessage: Message,
    private val editor: InteractionHook,
    private val id: String,
) {
    private val emojiPlus = Emoji.fromFormatted("<:plus:1232719440345563156>")
    private val emojiMinus = Emoji.fromFormatted("<:minus:1232718320621322260>")
    private val emojiButton = Emoji.fromUnicode("\uD83D\uDD18")
    private val emojiDropDown = Emoji.fromUnicode("\uD83D\uDD3D")
    private var state = EditorState.MENU

    private var message = originalMessage

    fun sendUpdate() {
        val data = when (state) {
            EditorState.MENU -> craftMenuComponents()
            EditorState.EDIT_BUTTON -> TODO()
            EditorState.EDIT_DROPDOWN -> TODO()
        }
        editor.editMessage(content = " ", embeds = listOf(data.first))
    }

    //
    // Editor Message Factory
    //
    private fun craftMenuComponents(): Pair<MessageEmbed, Set<ActionRow>> {
        return Embed {
            description = "Menu"
        } to setOf(
            ActionRow.of(StringSelectMenu("MCE:MENU:$id") {
                option("Add Button", "ADD:BUTTON", emoji = emojiPlus)
                option("Add Dropdown", "ADD:DROPDOWN", emoji = emojiPlus)
                message.components.forEach { cmp ->
//                    option("Edit ${cmp.type}")
                }
            })
        )
    }

    private enum class EditorState {
        MENU, EDIT_DROPDOWN, EDIT_BUTTON
    }
}