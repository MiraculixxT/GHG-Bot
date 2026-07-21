package de.miraculixx.ghg_bot.utils.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

/**
 * Colors log lines by level: ERROR=red, WARN=yellow, INFO=white, DEBUG/TRACE=gray.
 * Registered as the `levelcolor` conversion word in logback.xml.
 */
class LogHighlighter : ForegroundCompositeConverterBase<ILoggingEvent>() {
    override fun getForegroundColorCode(event: ILoggingEvent): String = when (event.level.toInt()) {
        Level.ERROR_INT -> ANSIConstants.RED_FG
        Level.WARN_INT -> ANSIConstants.YELLOW_FG
        Level.INFO_INT -> ANSIConstants.WHITE_FG
        else -> "90" // bright black = gray (DEBUG / TRACE)
    }
}
