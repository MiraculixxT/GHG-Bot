package de.miraculixx.ghg_bot.modules.message_editor.data

import kotlinx.serialization.Serializable

@Serializable
data class RawButton(
    val id: String,
    val name: String,
    val emoji: String,
    val mainAction: InteractionAction,
    val extraActions: List<InteractionAction>
)

