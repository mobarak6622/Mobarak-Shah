package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.domain.AngleMode
import com.example.domain.CalculationRecord
import com.example.domain.CalculatorAction
import com.example.domain.CalculatorUiState
import com.example.domain.EvaluationResult
import com.example.domain.MathEngine
import com.example.domain.NotationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var recordCounter = 1L
    private var justEvaluated = false

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.InputDigit -> appendDigit(action.digit)
            CalculatorAction.InputDecimal -> appendDecimal()
            is CalculatorAction.InputOperator -> appendOperator(action.operator)
            is CalculatorAction.InputPrefixFunction -> appendPrefixFunction(action.functionToken)
            is CalculatorAction.InputPostfixToken -> appendPostfixToken(action.token)
            is CalculatorAction.InputConstant -> appendConstant(action.symbol)
            CalculatorAction.InputOpenParen -> appendOpenParen()
            CalculatorAction.InputCloseParen -> appendCloseParen()
            CalculatorAction.ToggleSign -> toggleSign()
            CalculatorAction.Backspace -> backspace()
            CalculatorAction.ClearAll -> clearAll()
            CalculatorAction.Evaluate -> evaluateFinal()
            CalculatorAction.ToggleAngleMode -> cycleAngleMode()
            CalculatorAction.ToggleNotationMode -> cycleNotationMode()
            CalculatorAction.ToggleScientificDrawer -> {
                _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
            }
            CalculatorAction.ToggleSecondFunction -> {
                _uiState.update { it.copy(isSecondFunctionActive = !it.isSecondFunctionActive) }
            }
            CalculatorAction.MemoryClear -> {
                _uiState.update { it.copy(memoryValue = 0.0, hasMemory = false) }
            }
            CalculatorAction.MemoryRecall -> recallMemory()
            CalculatorAction.MemoryAdd -> adjustMemory(add = true)
            CalculatorAction.MemorySubtract -> adjustMemory(add = false)
            CalculatorAction.MemoryStore -> storeMemory()
            is CalculatorAction.ToggleHistorySheet -> {
                _uiState.update {
                    it.copy(
                        isHistorySheetOpen = action.open,
                        selectedSheetTab = action.initialTab
                    )
                }
            }
            is CalculatorAction.LoadExpression -> {
                justEvaluated = false
                updateExpression(action.expression, closeSheet = true)
            }
            is CalculatorAction.InsertHistoryResult -> {
                val sanitized = action.result.replace(",", "").replace(" ", "")
                val current = if (justEvaluated) "" else _uiState.value.expression
                justEvaluated = false
                updateExpression(current + sanitized, closeSheet = true)
            }
            is CalculatorAction.TogglePinHistory -> togglePinHistory(action.id)
            is CalculatorAction.DeleteHistoryItem -> deleteHistoryItem(action.id)
            CalculatorAction.ClearUnpinnedHistory -> {
                _uiState.update { state ->
                    state.copy(history = state.history.filter { it.isPinned })
                }
            }
        }
    }

    private fun appendDigit(digit: String) {
        val current = if (justEvaluated) "" else _uiState.value.expression
        justEvaluated = false
        if (current == "0" && digit == "0") return
        val next = if (current == "0") digit else current + digit
        updateExpression(next)
    }

    private fun appendDecimal() {
        val current = if (justEvaluated) "" else _uiState.value.expression
        justEvaluated = false
        // Check the current trailing numeric segment to avoid duplicate decimal points
        val lastNumberSegment = current.takeLastWhile { it.isDigit() || it == '.' }
        if (lastNumberSegment.contains('.')) return
        val prefix = if (lastNumberSegment.isEmpty()) "${current}0" else current
        updateExpression("$prefix.")
    }

    private fun appendOperator(operator: String) {
        var current = _uiState.value.expression
        if (current.isEmpty()) {
            if (operator == "−" || operator == "-") {
                updateExpression("−")
            } else if (_uiState.value.lastAnswer != 0.0) {
                justEvaluated = false
                updateExpression("Ans$operator")
            }
            return
        }
        justEvaluated = false
        // Replace trailing binary operator if one already exists
        val trailingOps = listOf("+", "−", "-", "×", "÷", "^", "ʸ√", "mod", "P", "C")
        val matchedTrailing = trailingOps.firstOrNull { current.endsWith(it) }
        if (matchedTrailing != null) {
            // Allow negative sign after ×, ÷, ^
            if ((operator == "−" || operator == "-") &&
                (matchedTrailing == "×" || matchedTrailing == "÷" || matchedTrailing == "^")
            ) {
                updateExpression("$current−")
                return
            }
            current = current.removeSuffix(matchedTrailing)
        }
        if (current.isNotEmpty() && current.last() != '(') {
            updateExpression(current + operator)
        } else if (current.endsWith("(") && (operator == "−" || operator == "-")) {
            updateExpression(current + "−")
        }
    }

    private fun appendPrefixFunction(functionToken: String) {
        val current = if (justEvaluated) "" else _uiState.value.expression
        justEvaluated = false
        updateExpression(current + functionToken)
    }

    private fun appendPostfixToken(token: String) {
        val current = _uiState.value.expression
        if (current.isEmpty()) return
        val lastChar = current.last()
        if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == 'φ' || current.endsWith("Ans")) {
            justEvaluated = false
            updateExpression(current + token)
        }
    }

    private fun appendConstant(symbol: String) {
        val current = if (justEvaluated) "" else _uiState.value.expression
        justEvaluated = false
        updateExpression(current + symbol)
    }

    private fun appendOpenParen() {
        val current = if (justEvaluated) "" else _uiState.value.expression
        justEvaluated = false
        updateExpression("$current(")
    }

    private fun appendCloseParen() {
        val current = _uiState.value.expression
        val unclosed = MathEngine.countUnclosedParentheses(current)
        if (unclosed > 0 && !current.endsWith("(")) {
            justEvaluated = false
            updateExpression("$current)")
        }
    }

    private fun toggleSign() {
        val current = _uiState.value.expression
        if (current.isEmpty()) {
            updateExpression("−")
            return
        }
        justEvaluated = false
        if (current.startsWith("−")) {
            updateExpression(current.removePrefix("−"))
        } else {
            updateExpression("−$current")
        }
    }

    private fun backspace() {
        val current = _uiState.value.expression
        if (current.isEmpty()) return
        justEvaluated = false

        val multiCharTokens = listOf(
            "sinh⁻¹(", "cosh⁻¹(", "tanh⁻¹(",
            "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
            "sinh(", "cosh(", "tanh(",
            "log₂(", "log(", "sin(", "cos(", "tan(", "abs(", "ln(",
            "10^(", "2^(", "e^(", "√(", "∛(",
            "mod", "Ans", "⁻¹", "ʸ√"
        )
        val matched = multiCharTokens.firstOrNull { current.endsWith(it) }
        val next = if (matched != null) {
            current.removeSuffix(matched)
        } else {
            current.dropLast(1)
        }
        updateExpression(next)
    }

    private fun clearAll() {
        justEvaluated = false
        _uiState.update {
            it.copy(
                expression = "",
                ghostClosingParens = "",
                livePreview = "",
                errorMessage = null
            )
        }
    }

    private fun evaluateFinal() {
        val state = _uiState.value
        if (state.expression.isBlank()) return

        val eval = MathEngine.evaluate(
            rawExpression = state.expression,
            angleMode = state.angleMode,
            notationMode = state.notationMode,
            lastAnswer = state.lastAnswer
        )

        when (eval) {
            is EvaluationResult.Success -> {
                val unclosed = MathEngine.countUnclosedParentheses(state.expression)
                val completedExpr = state.expression + ")".repeat(unclosed)
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                val newRecord = CalculationRecord(
                    id = recordCounter++,
                    expression = completedExpr,
                    result = eval.formatted,
                    rawValue = eval.value,
                    angleMode = state.angleMode,
                    timestampLabel = timestamp
                )
                val plainExpressionForEdit = MathEngine.formatNumber(eval.value, NotationMode.NORMAL)
                    .replace(",", "")

                justEvaluated = true
                _uiState.update { current ->
                    val updatedHistory = buildList {
                        addAll(current.history.filter { it.isPinned })
                        add(newRecord)
                        addAll(current.history.filter { !it.isPinned })
                    }
                    current.copy(
                        expression = plainExpressionForEdit,
                        ghostClosingParens = "",
                        livePreview = eval.formatted,
                        errorMessage = null,
                        lastAnswer = eval.value,
                        lastAnswerFormatted = eval.formatted,
                        history = updatedHistory
                    )
                }
            }
            is EvaluationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = eval.message,
                        livePreview = ""
                    )
                }
            }
            EvaluationResult.Empty -> Unit
        }
    }

    private fun cycleAngleMode() {
        val nextAngle = _uiState.value.angleMode.next()
        _uiState.update { it.copy(angleMode = nextAngle) }
        recomputePreview()
    }

    private fun cycleNotationMode() {
        val nextNotation = _uiState.value.notationMode.next()
        _uiState.update { it.copy(notationMode = nextNotation) }
        recomputePreview()
    }

    private fun resolveCurrentNumericValue(): Double? {
        val state = _uiState.value
        if (state.expression.isBlank()) return state.lastAnswer
        val eval = MathEngine.evaluate(
            rawExpression = state.expression,
            angleMode = state.angleMode,
            notationMode = state.notationMode,
            lastAnswer = state.lastAnswer
        )
        return (eval as? EvaluationResult.Success)?.value
    }

    private fun recallMemory() {
        val state = _uiState.value
        if (!state.hasMemory) return
        val memStr = MathEngine.formatNumber(state.memoryValue, NotationMode.NORMAL).replace(",", "")
        val current = if (justEvaluated) "" else state.expression
        justEvaluated = false
        updateExpression(current + memStr)
    }

    private fun adjustMemory(add: Boolean) {
        val value = resolveCurrentNumericValue() ?: return
        _uiState.update { state ->
            val updated = if (add) state.memoryValue + value else state.memoryValue - value
            state.copy(memoryValue = updated, hasMemory = true)
        }
    }

    private fun storeMemory() {
        val value = resolveCurrentNumericValue() ?: return
        _uiState.update { state ->
            state.copy(memoryValue = value, hasMemory = true)
        }
    }

    private fun togglePinHistory(id: Long) {
        _uiState.update { state ->
            val toggled = state.history.map { item ->
                if (item.id == id) item.copy(isPinned = !item.isPinned) else item
            }.sortedWith(compareByDescending<CalculationRecord> { it.isPinned }.thenByDescending { it.id })
            state.copy(history = toggled)
        }
    }

    private fun deleteHistoryItem(id: Long) {
        _uiState.update { state ->
            state.copy(history = state.history.filterNot { it.id == id })
        }
    }

    private fun updateExpression(newExpr: String, closeSheet: Boolean = false) {
        val state = _uiState.value
        val unclosed = MathEngine.countUnclosedParentheses(newExpr)
        val ghost = if (unclosed > 0) ")".repeat(unclosed) else ""

        // Strip trailing binary operator when computing live preview so typing "12+" still previews "12"
        val previewCandidate = stripTrailingOperatorsForPreview(newExpr)
        val previewResult = if (previewCandidate.isNotBlank()) {
            MathEngine.evaluate(
                rawExpression = previewCandidate,
                angleMode = state.angleMode,
                notationMode = state.notationMode,
                lastAnswer = state.lastAnswer
            )
        } else {
            EvaluationResult.Empty
        }

        val previewString = when (previewResult) {
            is EvaluationResult.Success -> previewResult.formatted
            else -> ""
        }

        _uiState.update {
            it.copy(
                expression = newExpr,
                ghostClosingParens = ghost,
                livePreview = previewString,
                errorMessage = null,
                isHistorySheetOpen = if (closeSheet) false else it.isHistorySheetOpen
            )
        }
    }

    private fun recomputePreview() {
        updateExpression(_uiState.value.expression)
    }

    private fun stripTrailingOperatorsForPreview(expr: String): String {
        var s = expr.trimEnd()
        val trailingOps = listOf("+", "−", "-", "×", "÷", "^", "ʸ√", "mod", "P", "C", "(")
        var changed = true
        while (changed && s.isNotEmpty()) {
            changed = false
            for (op in trailingOps) {
                if (s.endsWith(op)) {
                    s = s.removeSuffix(op)
                    changed = true
                }
            }
        }
        return s
    }
}
