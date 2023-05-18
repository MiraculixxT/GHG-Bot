package de.miraculixx.ghg_bot.utils.cache

import de.miraculixx.ghg_bot.JDA

val guildGHG = JDA.getGuildById(484676017513037844)!!
val ticketArchive = guildGHG.getTextChannelById(859832281300467792)!!
val ticketRole = guildGHG.getRoleById(1059456352222711860)!!
const val ticketChannelID = 859833491251789844