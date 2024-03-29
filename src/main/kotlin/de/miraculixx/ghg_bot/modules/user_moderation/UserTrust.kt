package de.miraculixx.ghg_bot.modules.user_moderation

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

@Serializable
data class UserTrust(
    val userID: Long,
    var level: Int,
    var points: Int
) {
    fun getCurrentLevelMax() = (100 * 1.25f.pow(level)).toInt()

    fun getLevelMultiplier() = when (level) {
        in Int.MIN_VALUE .. -1 -> 0.5f
        0 -> 1.0f
        1 -> 1.25f
        2 -> 1.5f
        3 -> 1.75f
        4 -> 2.0f
        5 -> 2.25f
        else -> 2.5f
    }

    fun getLevelPercentage(): Int {
        val currentLevelLimit = getCurrentLevelMax()
        return ((points.toFloat() / currentLevelLimit) * 100).roundToInt()
    }

    fun addPoints(p: Int) {
        points += p
        val currentLevelLimit = getCurrentLevelMax()
        if (points >= currentLevelLimit) {
            level++
            points -= currentLevelLimit
        }
    }

    fun removePoints(p: Int) {
        points -= p
        if (points < 0) {
            level--
            points = (100 * 1.25f.pow(level)).toInt().coerceAtLeast(100)
        }
    }
}