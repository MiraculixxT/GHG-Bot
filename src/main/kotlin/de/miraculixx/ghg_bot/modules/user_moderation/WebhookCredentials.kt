package de.miraculixx.ghg_bot.modules.user_moderation

import kotlinx.serialization.Serializable

@Serializable
data class WebhookCredentials(
    val innocent: String,
    val guilty: String
)
