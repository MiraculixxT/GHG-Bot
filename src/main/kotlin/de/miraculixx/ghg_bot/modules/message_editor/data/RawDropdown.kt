package de.miraculixx.ghg_bot.modules.message_editor.data

import dev.minn.jda.ktx.interactions.components.EntitySelectMenu
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.channel.ChannelType

@Serializable
data class RawDropdownString(
    val id: String,
    val isDisabled: Boolean = false,
    val maxValues: Int = 1,
    val minValues: Int = 1,
    val placeholder: String? = null,
    val options: List<RawDropdownOption>
)

@Serializable
data class RawDropdownOption(
    val label: String,
    val value: String,
    val description: String? = null,
    val emoji: String? = null,
    val active: Boolean = false,
    val action: InteractionAction
)

@Serializable
data class RawDropdownRoles(
    val id: String,
    val isDisabled: Boolean = false,
    val maxValues: Int = 1,
    val minValues: Int = 1,
    val placeholder: String? = null,
) {
    val a = EntitySelectMenu("id", setOf(net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE)) {
        this.setChannelTypes(ChannelType.VOICE)
    }
}

sealed interface Inter
