package de.miraculixx.ghg_bot.modules.message_editor.data

enum class InteractionType(val mainAction: Boolean) {
    RESPOND_MESSAGE(true),
    RESPOND_MODAL(true),

    REMOVE_ROLE(false),
    ADD_ROLE(false),
    TOGGLE_ROLE(false)
}
