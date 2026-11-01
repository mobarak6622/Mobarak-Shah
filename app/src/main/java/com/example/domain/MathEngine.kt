package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

object MathEngine {

    private const val GOLDEN_RATIO = 1.618033988749895

    private sealed interface Token {
        data class NumberLiteral(val value: Double) : Token
        data class UnaryFunc(val name: String) : Token
        data class BinaryOp(val op: String, val precedence: Int, val rightAssociative: Boolean = false) : Token
        data class PostfixOp(val op: String) : Token
        data object LeftParen : Token
        data object RightParen : Token
    }

    /**
     * Computes unmatched opening parentheses so the UI can display ghost closing parens
     * and the live preview can auto-close them cleanly.
     */
    fun countUnclosedParentheses(expression: String): Int {
        var openCount = 0
        for (ch in expression) {
            if (ch == '(') openCount++
            else if (ch == ')' && openCount > 0) openCount--
        }
        return openCount
    }

    fun evaluate(
        rawExpression: String,
        angleMode: AngleMode = AngleMode.DEG,
        notationMode: NotationMode = NotationMode.NORMAL,
        lastAnswer: Double = 0.0
    ): EvaluationResult {
        val trimmed = rawExpression.trim()
        if (trimmed.isEmpty()) return EvaluationResult.Empty

        // Strip trailing incomplete binary operators for live preview if needed by caller,
        // or return a clear error on explicit evaluation.
        val unclosed = countUnclosedParentheses(trimmed)
        val balancedExpression = if (unclosed > 0) {
            trimmed + ")".repeat(unclosed)
        } else {
            trimmed
        }

        return try {
            val tokens = tokenize(balancedExpression, lastAnswer)
            if (tokens.isEmpty()) return EvaluationResult.Empty
            val parser = Parser(tokens, angleMode)
            val result = parser.parseExpression()
            if (parser.hasRemainingTokens()) {
                return EvaluationResult.Error("Syntax error")
            }
            if (result.isNaN()) {
                return EvaluationResult.Error("Domain error")
            }
            if (result.isInfinite()) {
                return EvaluationResult.Error("Overflow / Division by 0")
            }
            // Snap near-zero floating point artifacts (like cos(90 deg) = 6.12e-17) to 0.0
            val cleanResult = if (abs(result) < 1e-14 && result != 0.0 && !trimmed.contains("E-")) {
                0.0
            } else {
                result
            }
            EvaluationResult.Success(
                value = cleanResult,
                formatted = formatNumber(cleanResult, notationMode)
            )
        } catch (e: MathDomainException) {
            EvaluationResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvaluationResult.Error("Syntax error")
        }
    }

    fun formatNumber(value: Double, notationMode: NotationMode = NotationMode.NORMAL): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "∞" else "−∞"
        if (abs(value) == 0.0) return "0"

        return when (notationMode) {
            NotationMode.NORMAL -> formatStandard(value)
            NotationMode.SCIENTIFIC -> formatScientific(value)
            NotationMode.ENGINEERING -> formatEngineering(value)
        }
    }

    private fun formatStandard(value: Double): String {
        val absVal = abs(value)
        if (absVal >= 1e12 || (absVal > 0 && absVal < 1e-7)) {
            return formatScientific(value)
        }
        val bd = BigDecimal.valueOf(value).round(MathContext(12, RoundingMode.HALF_UP)).stripTrailingZeros()
        val plain = bd.toPlainString()
        val parts = plain.split(".")
        val intPart = parts[0]
        val sign = if (intPart.startsWith("-")) "−" else ""
        val digits = intPart.removePrefix("-")
        val groupedInt = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (parts.size > 1) {
            "$sign$groupedInt.${parts[1]}"
        } else {
            "$sign$groupedInt"
        }
    }

    private fun formatScientific(value: Double): String {
        val formatted = String.format(Locale.US, "%.6E", value)
        val parts = formatted.split("E")
        val mantissa = BigDecimal(parts[0]).stripTrailingZeros().toPlainString().replace("-", "−")
        val exponent = parts[1].toInt()
        return "$mantissa × 10${toSuperscript(exponent)}"
    }

    private fun formatEngineering(value: Double): String {
        val sign = if (value < 0) "−" else ""
        val absVal = abs(value)
        val rawExp = floor(log10(absVal)).toInt()
        val engExp = rawExp - Math.floorMod(rawExp, 3)
        val mantissaVal = absVal / 10.0.pow(engExp)
        val mantissa = BigDecimal.valueOf(mantissaVal)
            .setScale(6, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return "$sign$mantissa × 10${toSuperscript(engExp)}"
    }

    private fun toSuperscript(number: Int): String {
        val map = mapOf(
            '-' to '⁻', '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³',
            '4' to '⁴', '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
        )
        return number.toString().map { map[it] ?: it }.joinToString("")
    }

    private class MathDomainException(message: String) : RuntimeException(message)

    private fun tokenize(expr: String, lastAnswer: Double): List<Token> {
        val rawTokens = mutableListOf<Token>()
        var i = 0
        val n = expr.length

        while (i < n) {
            val c = expr[i]
            when {
                c.isWhitespace() || c == ',' -> i++
                c == '(' -> {
                    rawTokens.add(Token.LeftParen)
                    i++
                }
                c == ')' -> {
                    rawTokens.add(Token.RightParen)
                    i++
                }
                c == 'π' -> {
                    rawTokens.add(Token.NumberLiteral(Math.PI))
                    i++
                }
                c == 'φ' -> {
                    rawTokens.add(Token.NumberLiteral(GOLDEN_RATIO))
                    i++
                }
                c == '!' -> {
                    rawTokens.add(Token.PostfixOp("!"))
                    i++
                }
                c == '%' -> {
                    rawTokens.add(Token.PostfixOp("%"))
                    i++
                }
                c == '²' -> {
                    rawTokens.add(Token.PostfixOp("²"))
                    i++
                }
                c == '³' -> {
                    rawTokens.add(Token.PostfixOp("³"))
                    i++
                }
                expr.startsWith("⁻¹", i) -> {
                    rawTokens.add(Token.PostfixOp("⁻¹"))
                    i += 2
                }
                expr.startsWith("ʸ√", i) -> {
                    rawTokens.add(Token.BinaryOp("ʸ√", precedence = 4, rightAssociative = true))
                    i += 2
                }
                c == '√' -> {
                    rawTokens.add(Token.UnaryFunc("sqrt"))
                    i++
                }
                c == '∛' -> {
                    rawTokens.add(Token.UnaryFunc("cbrt"))
                    i++
                }
                c == '+' -> {
                    rawTokens.add(Token.BinaryOp("+", 1))
                    i++
                }
                c == '−' || c == '-' -> {
                    // Check if unary negation
                    val prev = rawTokens.lastOrNull()
                    if (prev == null || prev is Token.LeftParen || prev is Token.BinaryOp || prev is Token.UnaryFunc) {
                        rawTokens.add(Token.UnaryFunc("neg"))
                    } else {
                        rawTokens.add(Token.BinaryOp("-", 1))
                    }
                    i++
                }
                c == '×' || c == '*' -> {
                    rawTokens.add(Token.BinaryOp("*", 2))
                    i++
                }
                c == '÷' || c == '/' -> {
                    rawTokens.add(Token.BinaryOp("/", 2))
                    i++
                }
                c == '^' -> {
                    rawTokens.add(Token.BinaryOp("^", 4, rightAssociative = true))
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val start = i
                    var hasDot = false
                    var hasExp = false
                    while (i < n) {
                        val ch = expr[i]
                        if (ch.isDigit()) {
                            i++
                        } else if (ch == '.' && !hasDot && !hasExp) {
                            hasDot = true
                            i++
                        } else if ((ch == 'E' || ch == 'e') && !hasExp && i + 1 < n &&
                            (expr[i + 1].isDigit() || expr[i + 1] == '+' || expr[i + 1] == '-' || expr[i + 1] == '−')
                        ) {
                            hasExp = true
                            i++
                            if (expr[i] == '+' || expr[i] == '-' || expr[i] == '−') {
                                i++
                            }
                        } else {
                            break
                        }
                    }
                    val numStr = expr.substring(start, i).replace('−', '-')
                    val parsed = numStr.toDoubleOrNull()
                        ?: throw MathDomainException("Invalid number")
                    rawTokens.add(Token.NumberLiteral(parsed))
                }
                else -> {
                    // Match multi-character identifiers / functions
                    when {
                        expr.startsWith("sinh⁻¹", i) -> {
                            rawTokens.add(Token.UnaryFunc("asinh"))
                            i += 6
                        }
                        expr.startsWith("cosh⁻¹", i) -> {
                            rawTokens.add(Token.UnaryFunc("acosh"))
                            i += 6
                        }
                        expr.startsWith("tanh⁻¹", i) -> {
                            rawTokens.add(Token.UnaryFunc("atanh"))
                            i += 6
                        }
                        expr.startsWith("sin⁻¹", i) -> {
                            rawTokens.add(Token.UnaryFunc("asin"))
                            i += 5
                        }
                        expr.startsWith("cos⁻¹", i) -> {
                            rawTokens.add(Token.UnaryFunc("acos"))
                            i += 5
                        }
                        expr.startsWith("tan⁻¹", i) -> {
                            rawTokens.add(Token.UnaryFunc("atan"))
                            i += 5
                        }
                        expr.startsWith("asinh", i) -> {
                            rawTokens.add(Token.UnaryFunc("asinh"))
                            i += 5
                        }
                        expr.startsWith("acosh", i) -> {
                            rawTokens.add(Token.UnaryFunc("acosh"))
                            i += 5
                        }
                        expr.startsWith("atanh", i) -> {
                            rawTokens.add(Token.UnaryFunc("atanh"))
                            i += 5
                        }
                        expr.startsWith("sinh", i) -> {
                            rawTokens.add(Token.UnaryFunc("sinh"))
                            i += 4
                        }
                        expr.startsWith("cosh", i) -> {
                            rawTokens.add(Token.UnaryFunc("cosh"))
                            i += 4
                        }
                        expr.startsWith("tanh", i) -> {
                            rawTokens.add(Token.UnaryFunc("tanh"))
                            i += 4
                        }
                        expr.startsWith("asin", i) -> {
                            rawTokens.add(Token.UnaryFunc("asin"))
                            i += 4
                        }
                        expr.startsWith("acos", i) -> {
                            rawTokens.add(Token.UnaryFunc("acos"))
                            i += 4
                        }
                        expr.startsWith("atan", i) -> {
                            rawTokens.add(Token.UnaryFunc("atan"))
                            i += 4
                        }
                        expr.startsWith("sin", i) -> {
                            rawTokens.add(Token.UnaryFunc("sin"))
                            i += 3
                        }
                        expr.startsWith("cos", i) -> {
                            rawTokens.add(Token.UnaryFunc("cos"))
                            i += 3
                        }
                        expr.startsWith("tan", i) -> {
                            rawTokens.add(Token.UnaryFunc("tan"))
                            i += 3
                        }
                        expr.startsWith("log₂", i) || expr.startsWith("log2", i) -> {
                            rawTokens.add(Token.UnaryFunc("log2"))
                            i += 4
                        }
                        expr.startsWith("log", i) -> {
                            rawTokens.add(Token.UnaryFunc("log10"))
                            i += 3
                        }
                        expr.startsWith("ln", i) -> {
                            rawTokens.add(Token.UnaryFunc("ln"))
                            i += 2
                        }
                        expr.startsWith("abs", i) -> {
                            rawTokens.add(Token.UnaryFunc("abs"))
                            i += 3
                        }
                        expr.startsWith("sqrt", i) -> {
                            rawTokens.add(Token.UnaryFunc("sqrt"))
                            i += 4
                        }
                        expr.startsWith("cbrt", i) -> {
                            rawTokens.add(Token.UnaryFunc("cbrt"))
                            i += 4
                        }
                        expr.startsWith("mod", i) -> {
                            rawTokens.add(Token.BinaryOp("mod", 2))
                            i += 3
                        }
                        expr.startsWith("Ans", i) -> {
                            rawTokens.add(Token.NumberLiteral(lastAnswer))
                            i += 3
                        }
                        c == 'P' -> {
                            rawTokens.add(Token.BinaryOp("P", 3))
                            i++
                        }
                        c == 'C' -> {
                            rawTokens.add(Token.BinaryOp("C", 3))
                            i++
                        }
                        c == 'e' -> {
                            rawTokens.add(Token.NumberLiteral(Math.E))
                            i++
                        }
                        else -> throw MathDomainException("Unexpected symbol '$c'")
                    }
                }
            }
        }

        // Insert implicit multiplication tokens where mathematically standard:
        // e.g. 2π, 3sin(45), (2+3)(4+5), 2(3), (2)3, 5!2
        val result = mutableListOf<Token>()
        for (idx in rawTokens.indices) {
            val current = rawTokens[idx]
            if (idx > 0) {
                val prev = rawTokens[idx - 1]
                val prevIsValue = prev is Token.NumberLiteral || prev is Token.RightParen || prev is Token.PostfixOp
                val currStartsValue = current is Token.NumberLiteral ||
                    current is Token.LeftParen ||
                    (current is Token.UnaryFunc && current.name != "neg")
                if (prevIsValue && currStartsValue) {
                    result.add(Token.BinaryOp("*", 2))
                }
            }
            result.add(current)
        }
        return result
    }

    private class Parser(
        private val tokens: List<Token>,
        private val angleMode: AngleMode
    ) {
        private var pos = 0

        fun hasRemainingTokens(): Boolean = pos < tokens.size

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun consume(): Token = tokens[pos++]

        fun parseExpression(minPrecedence: Int = 1): Double {
            var left = parsePrimary()

            while (true) {
                val next = peek()
                if (next is Token.BinaryOp && next.precedence >= minPrecedence) {
                    consume()
                    val nextMinPrec = if (next.rightAssociative) next.precedence else next.precedence + 1
                    val right = parseExpression(nextMinPrec)
                    left = applyBinaryOp(next.op, left, right)
                } else {
                    break
                }
            }
            return left
        }

        private fun parsePrimary(): Double {
            var value = parseAtom()
            // Apply any postfix operators (!, %, ², ³, ⁻¹) to the atom or function result
            while (peek() is Token.PostfixOp) {
                val post = consume() as Token.PostfixOp
                value = applyPostfixOp(post.op, value)
            }
            return value
        }

        private fun parseAtom(): Double {
            val token = peek() ?: throw MathDomainException("Incomplete expression")
            return when (token) {
                is Token.NumberLiteral -> {
                    consume()
                    token.value
                }
                is Token.LeftParen -> {
                    consume()
                    val inner = parseExpression(1)
                    if (peek() is Token.RightParen) {
                        consume()
                    }
                    inner
                }
                is Token.UnaryFunc -> {
                    consume()
                    val arg = if (token.name == "neg") {
                        parseExpression(2)
                    } else {
                        parseAtom()
                    }
                    applyUnaryFunc(token.name, arg)
                }
                else -> throw MathDomainException("Unexpected token")
            }
        }

        private fun toRadians(angle: Double): Double = when (angleMode) {
            AngleMode.DEG -> Math.toRadians(angle)
            AngleMode.RAD -> angle
            AngleMode.GRAD -> angle * (Math.PI / 200.0)
        }

        private fun fromRadians(rad: Double): Double = when (angleMode) {
            AngleMode.DEG -> Math.toDegrees(rad)
            AngleMode.RAD -> rad
            AngleMode.GRAD -> rad * (200.0 / Math.PI)
        }

        private fun applyUnaryFunc(name: String, arg: Double): Double = when (name) {
            "neg" -> -arg
            "sin" -> {
                val rad = toRadians(arg)
                // Handle exact multiples of 180 deg for clean 0
                if (angleMode == AngleMode.DEG && abs(arg % 180.0) == 0.0) 0.0 else sin(rad)
            }
            "cos" -> {
                val rad = toRadians(arg)
                if (angleMode == AngleMode.DEG && abs((abs(arg) - 90.0) % 180.0) == 0.0) 0.0 else cos(rad)
            }
            "tan" -> {
                if (angleMode == AngleMode.DEG && abs((abs(arg) - 90.0) % 180.0) == 0.0) {
                    throw MathDomainException("Domain error: tan(${arg.toInt()}°) undefined")
                }
                tan(toRadians(arg))
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw MathDomainException("Domain error: sin⁻¹ requires [−1, 1]")
                fromRadians(asin(arg))
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw MathDomainException("Domain error: cos⁻¹ requires [−1, 1]")
                fromRadians(acos(arg))
            }
            "atan" -> fromRadians(atan(arg))
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "asinh" -> asinh(arg)
            "acosh" -> {
                if (arg < 1.0) throw MathDomainException("Domain error: cosh⁻¹ requires x ≥ 1")
                acosh(arg)
            }
            "atanh" -> {
                if (arg <= -1.0 || arg >= 1.0) throw MathDomainException("Domain error: tanh⁻¹ requires (−1, 1)")
                atanh(arg)
            }
            "ln" -> {
                if (arg <= 0.0) throw MathDomainException("Domain error: ln(x) requires x > 0")
                ln(arg)
            }
            "log10" -> {
                if (arg <= 0.0) throw MathDomainException("Domain error: log(x) requires x > 0")
                log10(arg)
            }
            "log2" -> {
                if (arg <= 0.0) throw MathDomainException("Domain error: log₂(x) requires x > 0")
                log2(arg)
            }
            "sqrt" -> {
                if (arg < 0.0) throw MathDomainException("Domain error: √ of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            else -> throw MathDomainException("Unknown function $name")
        }

        private fun applyPostfixOp(op: String, arg: Double): Double = when (op) {
            "!" -> factorial(arg)
            "%" -> arg / 100.0
            "²" -> arg * arg
            "³" -> arg * arg * arg
            "⁻¹" -> {
                if (arg == 0.0) throw MathDomainException("Cannot divide by zero")
                1.0 / arg
            }
            else -> arg
        }

        private fun applyBinaryOp(op: String, left: Double, right: Double): Double = when (op) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> {
                if (right == 0.0) throw MathDomainException("Cannot divide by zero")
                left / right
            }
            "mod" -> {
                if (right == 0.0) throw MathDomainException("Cannot modulo by zero")
                left % right
            }
            "^" -> {
                val res = left.pow(right)
                if (res.isNaN()) throw MathDomainException("Domain error in power")
                res
            }
            "ʸ√" -> {
                if (left == 0.0) throw MathDomainException("Zeroth root is undefined")
                if (right < 0.0) {
                    val isOddInt = left % 1.0 == 0.0 && (left.roundToLong() % 2L != 0L)
                    if (!isOddInt) throw MathDomainException("Even root of negative number")
                    -((-right).pow(1.0 / left))
                } else {
                    right.pow(1.0 / left)
                }
            }
            "P" -> permutation(left, right)
            "C" -> combination(left, right)
            else -> throw MathDomainException("Unknown operator $op")
        }

        private fun factorial(x: Double): Double {
            if (x < 0.0 || x % 1.0 != 0.0) {
                throw MathDomainException("Factorial requires non-negative integer")
            }
            val n = x.roundToLong()
            if (n > 170L) {
                throw MathDomainException("Overflow: n! exceeds max double (170!)")
            }
            var acc = 1.0
            for (i in 2L..n) {
                acc *= i.toDouble()
            }
            return acc
        }

        private fun permutation(nVal: Double, rVal: Double): Double {
            if (nVal < 0 || rVal < 0 || nVal % 1.0 != 0.0 || rVal % 1.0 != 0.0 || rVal > nVal) {
                throw MathDomainException("nPr requires integers 0 ≤ r ≤ n")
            }
            val n = nVal.roundToLong()
            val r = rVal.roundToLong()
            if (n > 170L) throw MathDomainException("Overflow in nPr")
            var acc = 1.0
            for (i in (n - r + 1)..n) {
                acc *= i.toDouble()
            }
            return acc
        }

        private fun combination(nVal: Double, rVal: Double): Double {
            if (nVal < 0 || rVal < 0 || nVal % 1.0 != 0.0 || rVal % 1.0 != 0.0 || rVal > nVal) {
                throw MathDomainException("nCr requires integers 0 ≤ r ≤ n")
            }
            val n = nVal.roundToLong()
            val k = minOf(rVal.roundToLong(), n - rVal.roundToLong())
            var acc = 1.0
            for (i in 1L..k) {
                acc = acc * (n - k + i).toDouble() / i.toDouble()
            }
            return acc
        }
    }
}
