package de.miraculixx.ghg_bot.utils.cache

import de.miraculixx.ghg_bot.JDA
import de.miraculixx.ghg_bot.config.ConfigManager

private val specialChannels = ConfigManager.specialChannels

val guildGHG = JDA.getGuildById(specialChannels.guild)!!
val ticketArchive = guildGHG.getTextChannelById(specialChannels.tickets.archive)!!
val ticketReportRole = guildGHG.getRoleById(specialChannels.tickets.reportRole)!!
val ticketQuestionRole = guildGHG.getRoleById(specialChannels.tickets.questionRole)!!
val modLog = guildGHG.getTextChannelById(specialChannels.modLog)!!
val teamRole = guildGHG.getRoleById(specialChannels.teamRole)!!
val ticketChannelID = specialChannels.tickets.channel
