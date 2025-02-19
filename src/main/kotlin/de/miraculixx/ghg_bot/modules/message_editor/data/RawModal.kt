package de.miraculixx.ghg_bot.modules.message_editor.data

import kotlinx.serialization.Serializable

@Serializable
data class RawModal(
    val title: String,
    val inputs: List<RawModalInputs>
)

@Serializable
data class RawModalInputs(
    val id: String,
    val label: String,
    val required: Boolean,
    val value: String,
    val placeholder: String,
    val minCharacters: Int,
    val maxCharacters: Int
)