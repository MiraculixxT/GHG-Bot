package de.miraculixx.ghg_bot.modules.message_editor.data

import kotlinx.serialization.Serializable

@Serializable
data class InteractionModal(
    val id: String,
    val rawModal: RawModal,
    val action: InteractionAction // TODO: Create extra modal actions
): InteractionAction


@Serializable
sealed interface InteractionAction {

}