package de.miraculixx.ghg_bot.modules.auto_support

import de.miraculixx.ghg_bot.utils.cache.*
import net.dv8tion.jda.api.entities.MessageEmbed

enum class SupportFilter(val embed: MessageEmbed) {
    V4(embedV4),
    SPAM(embedAd),
    DEATHRUN(embedDeathrun),
    CHALLENGES(embedChallenges),
    TWITCH(embedStream),
    BADLION(embedBadlion),
    GOMME_CLAN(embedGomme)
}