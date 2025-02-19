package de.miraculixx.ghg_bot.utils.cache

import de.miraculixx.ghg_bot.JDA

val guildGHG = JDA.getGuildById(484676017513037844)!!
val ticketArchive = guildGHG.getTextChannelById(859832281300467792)!!
val ticketReportRole = guildGHG.getRoleById(1108734111943766016)!!
val ticketQuestionRole = guildGHG.getRoleById(1059456352222711860)!!
val modLog = guildGHG.getTextChannelById(537647200621166602)!!
val teamRole = guildGHG.getRoleById(805123617003798599)!!
const val ticketChannelID = 859833491251789844