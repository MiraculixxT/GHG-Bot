package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.modules.other.CommandOnlyChannel
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class StreamCommands: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        when (it.name) {
            "tastatur" -> it.sendResponse("Bastis Tastatur\n ► ROG Azoth https://ins.deals/BastiROG (Werbung)")
            "maus" -> it.sendResponse("Bastis Maus\n ► ROG Harpe Ace Aim Lab Edition https://ins.deals/BastiROG (Werbung)")
            "socials" -> it.sendResponse("Bastis Soziale Medien\n ► [Twitch](https://twitch.tv/bastighg)\n ► [YouTube - Main](https://youtube.com/kompetenzGHG)\n ► [YouTube - Zweitkanal](https://bit.ly/2b2TP84)\n ► [YouTube - Clips](https://youtube.com/BastiGHGClips)\n ► [Instagram](https://bit.ly/3EuanUO)\n ► [Twitter](https://twitter.com/kompetenzGHG)\n ► [TikTok](https://www.tiktok.com/@bastighg.tiktok)\n ► [WhatsApp Kanal](https://whatsapp.com/channel/0029VaDqdNuISTkMiLZLhm3O)")
            "merch" -> it.sendResponse("BastiGHG Merch\n ► https://bit.ly/2kkRlvQ #WERBUNG")
            "monitor" -> it.sendResponse( "Hauptmonitor\n ► ROG Strix XG259CM\n3 extra Monitore\n ► ROG Strix XG258Q (Werbung)")
            "mauspad" -> it.sendResponse("Bastis Mauspad\n ► ROG Sheath Electro Punk https://ins.deals/BastiROG (Werbung)")
            "mc-settings" -> it.sendResponse(" DPI ► 600 & 60% ingame\nFOV ► 77\nSounds ► https://i.imgur.com/nu2wvuk.png")
            "pc-specs" -> it.sendResponse("Bastis PC Specs\n ► https://imgur.com/a/h4F98Eh")
            "timolia" -> it.sendResponse("Wenn ihr euch auf [Timolia](https://shop.timolia.de/) einen Rang kaufen möchtet, dann bekommt ihr mit dem Code GHG 5 % Rabatt. #WERBUNG\n")
        }
    }

    private fun SlashCommandInteractionEvent.sendResponse(response: String) {
        val visible = CommandOnlyChannel.channels.contains(channel.idLong)
        reply_(embeds = listOf(Embed {
            color = 0xea46f1
            title = "Command >> ${name[0].uppercase()}${name.drop(1)}"
            description = response
        }), ephemeral = !visible).queue()
    }
}