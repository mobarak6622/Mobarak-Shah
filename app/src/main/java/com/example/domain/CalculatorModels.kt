package com.example.domain

enum class AngleMode(val label: String) {
    DEG("DEG"),
    RAD("RAD"),
    GRAD("GRAD");

    fun next(): AngleMode = when (this) {
        DEG -> RAD
        RAD -> GRAD
        GRAD -> DEG
    }
}

enum class NotationMode(val label: String) {
    NORMAL("DEC"),
    SCIENTIFIC("SCI"),
    ENGINEERING("ENG");

    fun next(): NotationMode = when (this) {
        NORMAL -> SCIENTIFIC
        SCIENTIFIC -> ENGINEERING
        ENGINEERING -> NORMAL
    }
}

data class PhysicalConstant(
    val symbol: String,
    val name: String,
    val expressionToken: String,
    val displayValue: String,
    val unit: String,
    val numericValue: Double
)

data class SampleFormula(
    val title: String,
    val category: String,
    val expression: String,
    val description: String
)

data class CalculationRecord(
    val id: Long,
    val expression: String,
    val result: String,
    val rawValue: Double,
    val angleMode: AngleMode,
    val timestampLabel: String,
    val isPinned: Boolean = false
)

sealed class EvaluationResult {
    data class Success(
        val value: Double,
        val formatted: String
    ) : EvaluationResult()

    data class Error(val message: String) : EvaluationResult()
    data object Empty : EvaluationResult()
}

sealed interface CalculatorAction {
    data class InputDigit(val digit: String) : CalculatorAction
    data object InputDecimal : CalculatorAction
    data class InputOperator(val operator: String) : CalculatorAction
    data class InputPrefixFunction(val functionToken: String) : CalculatorAction
    data class InputPostfixToken(val token: String) : CalculatorAction
    data class InputConstant(val symbol: String) : CalculatorAction
    data object InputOpenParen : CalculatorAction
    data object InputCloseParen : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Backspace : CalculatorAction
    data object ClearAll : CalculatorAction
    data object Evaluate : CalculatorAction
    data object ToggleAngleMode : CalculatorAction
    data object ToggleNotationMode : CalculatorAction
    data object ToggleScientificDrawer : CalculatorAction
    data object ToggleSecondFunction : CalculatorAction
    data object MemoryClear : CalculatorAction
    data object MemoryRecall : CalculatorAction
    data object MemoryAdd : CalculatorAction
    data object MemorySubtract : CalculatorAction
    data object MemoryStore : CalculatorAction
    data class ToggleHistorySheet(val open: Boolean, val initialTab: Int = 0) : CalculatorAction
    data class LoadExpression(val expression: String) : CalculatorAction
    data class InsertHistoryResult(val result: String) : CalculatorAction
    data class TogglePinHistory(val id: Long) : CalculatorAction
    data class DeleteHistoryItem(val id: Long) : CalculatorAction
    data object ClearUnpinnedHistory : CalculatorAction
}

data class CalculatorUiState(
    val expression: String = "",
    val ghostClosingParens: String = "",
    val livePreview: String = "",
    val errorMessage: String? = null,
    val lastAnswer: Double = 0.0,
    val lastAnswerFormatted: String = "0",
    val angleMode: AngleMode = AngleMode.DEG,
    val notationMode: NotationMode = NotationMode.NORMAL,
    val isScientificExpanded: Boolean = true,
    val isSecondFunctionActive: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val history: List<CalculationRecord> = emptyList(),
    val isHistorySheetOpen: Boolean = false,
    val selectedSheetTab: Int = 0
)

object ScientificCatalog {
    val constants = listOf(
        PhysicalConstant(
            symbol = "π",
            name = "Archimedes' Constant (Pi)",
            expressionToken = "π",
            displayValue = "3.14159265359",
            unit = "rad",
            numericValue = Math.PI
        ),
        PhysicalConstant(
            symbol = "e",
            name = "Euler's Number",
            expressionToken = "e",
            displayValue = "2.71828182846",
            unit = "",
            numericValue = Math.E
        ),
        PhysicalConstant(
            symbol = "φ",
            name = "Golden Ratio (Phi)",
            expressionToken = "φ",
            displayValue = "1.61803398875",
            unit = "",
            numericValue = 1.618033988749895
        ),
        PhysicalConstant(
            symbol = "c",
            name = "Speed of Light in Vacuum",
            expressionToken = "299792458",
            displayValue = "2.99792458 × 10⁸",
            unit = "m/s",
            numericValue = 299792458.0
        ),
        PhysicalConstant(
            symbol = "g",
            name = "Standard Gravity",
            expressionToken = "9.80665",
            displayValue = "9.80665",
            unit = "m/s²",
            numericValue = 9.80665
        ),
        PhysicalConstant(
            symbol = "h",
            name = "Planck's Constant",
            expressionToken = "6.62607015E-34",
            displayValue = "6.62607 × 10⁻³⁴",
            unit = "J·s",
            numericValue = 6.62607015e-34
        ),
        PhysicalConstant(
            symbol = "Nₐ",
            name = "Avogadro's Constant",
            expressionToken = "6.02214076E23",
            displayValue = "6.02214 × 10²³",
            unit = "mol⁻¹",
            numericValue = 6.02214076e23
        )
    )

    val sampleFormulas = listOf(
        SampleFormula(
            title = "Pythagorean Trigonometric Identity",
            category = "Trigonometry",
            expression = "sin(45)²+cos(45)²",
            description = "Verifies sin²(θ) + cos²(θ) = 1 at 45°"
        ),
        SampleFormula(
            title = "Golden Ratio Closed Form",
            category = "Number Theory",
            expression = "(1+√(5))÷2",
            description = "Exact algebraic representation of φ ≈ 1.6180339887"
        ),
        SampleFormula(
            title = "Combinations & Probability",
            category = "Combinatorics",
            expression = "10C3×5!",
            description = "10 choose 3 multiplied by 5 factorial"
        ),
        SampleFormula(
            title = "Logarithmic & Exponential Decay",
            category = "Calculus",
            expression = "ln(e^5)+log(1000)−log₂(64)",
            description = "Combines natural, base-10, and binary logarithms"
        ),
        SampleFormula(
            title = "Damped Harmonic Amplitude",
            category = "Physics",
            expression = "e^(−0.25)×cos(60)+∛(27)",
            description = "Exponential damping factor with cube root offset"
        )
    )
}
