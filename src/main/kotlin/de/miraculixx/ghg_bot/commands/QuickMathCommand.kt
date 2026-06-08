package de.miraculixx.ghg_bot.commands

import de.miraculixx.ghg_bot.utils.entities.ButtonEvent
import de.miraculixx.ghg_bot.utils.entities.ModalEvent
import de.miraculixx.ghg_bot.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.interactions.components.TextInput
import dev.minn.jda.ktx.interactions.components.replyModal
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.random.Random

object QuickMathCommand : SlashCommandEvent, ModalEvent, ButtonEvent {
    private const val PLAY_BUTTON_ID = "22142abbf1c74da187fdabd4b59d4456"
    private val challenges = ConcurrentHashMap<String, MathChallenge>()
    private val invisibleChars = listOf("\u200B", "\u200C", "\u200D", "\u2060")

    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        it.openChallenge(it.user.idLong)
    }

    override suspend fun trigger(it: ButtonInteractionEvent) {
        it.openChallenge(it.user.idLong)
    }

    private fun IModalCallback.openChallenge(userId: Long) {
        val startedAt = System.currentTimeMillis()
        val modalId = "QUICK-MATH:$startedAt:$userId:${Random.nextInt(100_000, 1_000_000)}"
        val challenge = createChallenge()
        challenges[modalId] = challenge

        replyModal(modalId, "Quick Math") {
            label("Aufgabe", description = challenge.hiddenQuestion) {
                child = TextInput("ANSWER", TextInputStyle.SHORT, placeholder = "Antwort als ganze Zahl") {
                    required = true
                    requiredLength = 1..20
                }
            }
        }.queue()
    }

    override suspend fun trigger(it: ModalInteractionEvent) {
        val parts = it.modalId.split(":")
        val startedAt = parts.getOrNull(1)?.toLongOrNull()
        val targetUserId = parts.getOrNull(2)?.toLongOrNull()
        val answer = it.getValue("ANSWER")?.asString?.trim()?.toLongOrNull()

        if (startedAt == null || targetUserId == null) {
            it.reply("Dieses Quick-Math Rätsel ist ungültig. Bitte starte `/quick-math` erneut.").queue()
            return
        }

        if (targetUserId != it.user.idLong) {
            it.reply("Dieses Quick-Math Rätsel gehört nicht dir.").queue()
            return
        }

        val challenge = challenges.remove(it.modalId)
        if (challenge == null) {
            it.reply("Dieses Quick-Math Rätsel ist abgelaufen. Bitte starte `/quick-math` erneut.").queue()
            return
        }

        val elapsedMs = System.currentTimeMillis() - startedAt
        val elapsed = "%.2f".format(elapsedMs / 1000.0)
        val success = answer == challenge.result
        val submitted = answer?.toString() ?: it.getValue("ANSWER")?.asString?.trim().orEmpty()

        it.reply(MessageCreateBuilder().addComponents(buildResultComponents(challenge, submitted, elapsed, success, targetUserId))
            .useComponentsV2().build()).queue()
    }

    private fun buildResultComponents(challenge: MathChallenge, submitted: String, elapsed: String, success: Boolean, user: Long): List<MessageTopLevelComponent> {
        val result = challenge.result.toString()
        val answerLine = if (success) {
            "- Richtige Antwort  >> `$result`\n- Zeit                           >> `${elapsed}s`"
        } else {
            "- Falsche Antwort  >> `${submitted.ifBlank { "<leer>" }}` (`$result`)"
        }

        return listOf(
            Container.of(
                Section.of(
                    Button.of(ButtonStyle.SUCCESS, PLAY_BUTTON_ID, "𝗣𝗟𝗔𝗬"),
                    TextDisplay.of("# 🎲 Quick Math")
                ),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("- Challenge       ${if (success) " " else ""}        >> `${challenge.cleanQuestion}`" +
                        "\n$answerLine\n-# Beantwortet von <@$user>")
            ).withAccentColor(if (success) 0x2ECC71 else 0xE74C3C)
        )
    }

    private fun createChallenge(): MathChallenge {
        repeat(50) {
            val expression = randomExpression(depth = Random.nextInt(2, 4), topLevel = true)
            val question = expression.render()
            if (question.length <= 34 && abs(expression.value) <= 100_000) {
                return MathChallenge(question, question.hideSpaces(), expression.value)
            }
        }

        val fallback = Binary(Const(38), Operator.ADD, Binary(Const(-8), Operator.MULTIPLY, Const(3), grouped = false), grouped = false)
        val question = fallback.render()
        return MathChallenge(question, question.hideSpaces(), fallback.value)
    }

    private fun randomExpression(depth: Int, topLevel: Boolean = false): Expr {
        if (depth <= 0) return Const(Random.nextInt(-12, 51).let { if (it == 0) 1 else it }.toLong())

        val op = randomOperator()
        val grouped = !topLevel && Random.nextBoolean()
        val left = randomExpression(depth - 1)
        var right = randomExpression(depth - 1)

        if (op == Operator.MODULO) {
            while (right.value == 0L) right = randomExpression(depth - 1)
        }

        return Binary(left, op, right, grouped)
    }

    private fun randomOperator(): Operator {
        return when (Random.nextInt(100)) {
            in 0..6 -> Operator.MODULO
            in 7..29 -> Operator.MULTIPLY
            in 30..61 -> Operator.SUBTRACT
            else -> Operator.ADD
        }
    }

    private fun String.hideSpaces(): String {
        return Regex(" ").replace(this) { " " + invisibleChars.shuffled().take(Random.nextInt(1, 4)).joinToString("") }
    }

    private data class MathChallenge(
        val cleanQuestion: String,
        val hiddenQuestion: String,
        val result: Long
    )

    private sealed interface Expr {
        val value: Long
        val precedence: Int
        fun render(parentPrecedence: Int = 0): String
    }

    private data class Const(override val value: Long) : Expr {
        override val precedence = 3
        override fun render(parentPrecedence: Int): String = value.toString()
    }

    private data class Binary(
        val left: Expr,
        val operator: Operator,
        val right: Expr,
        val grouped: Boolean
    ) : Expr {
        override val value: Long = when (operator) {
            Operator.ADD -> left.value + right.value
            Operator.SUBTRACT -> left.value - right.value
            Operator.MULTIPLY -> left.value * right.value
            Operator.MODULO -> left.value % right.value
        }
        override val precedence = operator.precedence

        override fun render(parentPrecedence: Int): String {
            val leftRendered = left.render(precedence)
            val rightRendered = right.render(precedence + 1)
            val rendered = "$leftRendered ${operator.symbol} $rightRendered"
            return if (grouped || precedence < parentPrecedence) "($rendered)" else rendered
        }
    }

    private enum class Operator(val symbol: String, val precedence: Int) {
        ADD("+", 1),
        SUBTRACT("-", 1),
        MULTIPLY("*", 2),
        MODULO("%", 2)
    }
}
