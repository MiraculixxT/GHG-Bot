package de.miraculixx.ghg_bot.utils.cache

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed


fun InlineEmbed.applyAutoFooter() {
    footer {
        name = "Diese Nachricht ist automatisiert - Reagiere mit \uD83D\uDDD1️sollte dies dir nicht helfen"
        iconUrl = "https://i.imgur.com/WBopBcV.png"
    }
}

val embedV4 = Embed {
    description = "Das **ZickZack v4 Texturepack** ist aktuell noch nicht öffentlich!\n" +
            "Wann oder ob es in der Zukunft veröffentlicht werden wird ist unklar, warte also einfach ab <:FeelsOkayMan:816743864379113472>"
    color = 0x9a00bc
    applyAutoFooter()
}

val embedAd = Embed {
    description = "Jegliche Werbung oder Scam-Links, die du per DM ``(Direct Message)`` erhältst **nicht** an uns senden über Tickets oder in allgemeinen Chats. \n" +
            "Reporte diese direkt an Discord, damit der Bot oder User als `spam` markiert wird und damit blockiert ist:"
    image = "https://i.imgur.com/cKQyjjg.png"
    color = 0xcc0000
    applyAutoFooter()
}

val embedDeathrun = Embed {
    description = "Du kannst durch Channel Points auf [Twitch](https://twitch.tv/bastighg) an Deathruns teilnehmen, sobald welche geplant sind.\n" +
            "Schaue einfach aktiv zu, damit du nicht verpasst dich zu registrieren! (Es gibt keinen genauen Plan wann Deathruns statt finden)"
    color = 0xcc0000
    applyAutoFooter()
}

val embedChallenges = Embed {
    description =
        "Basti hat keinen Challenge Server und auch kein öffentliches Challenge Plugin! Wenn du selbst welche spielen möchtest, musst du sie dir selbst erstellen oder frage in <#568922343611826186> nach, ob ein **sicheres** bekannt ist.\n" +
                "\n" +
                "**WARNUNG** " +
                "```diff\n- Gerade auf spigotmc treiben sich viele Plugins um, die primär darauf aus sind dir zu schaden!```"
    color = 0xcc0000
    applyAutoFooter()
}

val embedStream = Embed {
    description =
        "Basti streamed jeden Tag auf [Twitch](https://twitch.tv/bastighg). Ja, **JEDEN** Tag auch heute. Meist streamed er Abends ungefähr ab 18 Uhr, dies kann jedoch von Tag zu Tag variieren. Aber keine Angst, er wird auch heute nicht seine streak verlieren <:FeelsOkayMan:816743864379113472>"
    color = 0x9a00bc
    applyAutoFooter()
}

val embedBadlion = Embed {
    description = "Wir sind kein Badlion Support Server und stehen auch in keinster Verbindung damit. Versuche dein Glück im offiziellen Support auf der offiziellen Website"
    color = 0xcc0000
    applyAutoFooter()
}

val embedApplication = Embed {
    description = "Aktuell suchen wir keine neuen Moderatoren auf Twitch oder Discord. Jegliche Moderatoren werden von Basti persönlich ohne Bewerbungsphase ausgewählt, sofern er welche benötigt." +
            "\nDas selbe gilt für andere Stellen wie Cutter oder Developer¹!\n\n" +
            "*¹Sofern nicht explizit auf Twitter aufgefordert*"
    color = 0xcc0000
}

val embedGomme = Embed {
    description = "Der Gomme Clan ist aktuell und sehr wahrscheinlich auch in Zukunft voll. Früher wurde das Limit entfernt wodurch sehr viel Spieler beigetreten sind"
    color = 0xcc0000
    applyAutoFooter()
}