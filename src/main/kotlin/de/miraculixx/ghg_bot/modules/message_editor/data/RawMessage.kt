package de.miraculixx.ghg_bot.modules.message_editor.data

import kotlinx.serialization.Serializable

@Serializable
data class RawMessage(
    val content: String?,
    val embeds: List<RawEmbed>?
)

@Serializable
data class RawEmbed(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val color: Int? = null,
    val fields: List<RawEmbedField>? = null,
    val author: RawEmbedAuthor? = null,
    val footer: RawEmbedFooter? = null,
    val image: RawEmbedImage? = null,
    val thumbnail: RawEmbedImage? = null,
    val timestamp: String? = null
)

@Serializable
data class RawEmbedField(
    val name: String,
    val value: String,
    val inline: Boolean = false
)

@Serializable
data class RawEmbedAuthor(
    val name: String,
    val url: String? = null,
    val icon_url: String? = null
)

@Serializable
data class RawEmbedFooter(
    val text: String,
    val icon_url: String? = null
)

@Serializable
data class RawEmbedImage(
    val url: String
)
