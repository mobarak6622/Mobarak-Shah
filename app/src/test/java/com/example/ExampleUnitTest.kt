package com.example

import com.example.domain.AngleMode
import com.example.domain.EvaluationResult
import com.example.domain.MathEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun pythagoreanIdentity_inDegrees_isOne() {
        val result = MathEngine.evaluate("sin(45)²+cos(45)²", AngleMode.DEG)
        assertTrue(result is EvaluationResult.Success)
        assertEquals(1.0, (result as EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun combinatoricsAndFactorial_evaluatesAccurately() {
        val result = MathEngine.evaluate("10C3×5!", AngleMode.DEG)
        assertTrue(result is EvaluationResult.Success)
        assertEquals(14400.0, (result as EvaluationResult.Success).value, 1e-6)
    }

    @Test
    fun implicitMultiplicationAndUnclosedParens_autoBalances() {
        val result = MathEngine.evaluate("2(3+4", AngleMode.DEG)
        assertTrue(result is EvaluationResult.Success)
        assertEquals(14.0, (result as EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun logarithmsAndRoots_evaluateCorrectly() {
        val result = MathEngine.evaluate("ln(e^5)+log(1000)−log₂(64)+∛(27)", AngleMode.RAD)
        assertTrue(result is EvaluationResult.Success)
        // 5 + 3 - 6 + 3 = 5
        assertEquals(5.0, (result as EvaluationResult.Success).value, 1e-9)
    }

    @Test
    fun divisionByZero_returnsDomainError() {
        val result = MathEngine.evaluate("42÷0", AngleMode.DEG)
        assertTrue(result is EvaluationResult.Error)
    }
}
