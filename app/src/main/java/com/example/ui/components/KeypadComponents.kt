package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.CalculatorAction
import com.example.domain.CalculatorUiState
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.ElectricCyanDim
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianDisplayBorder
import com.example.ui.theme.ObsidianElevatedKey
import com.example.ui.theme.ObsidianScientificKey
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.OnElectricCyan
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer

data class KeySpec(
    val label: String,
    val testTag: String,
    val action: CalculatorAction,
    val style: KeyVisualStyle = KeyVisualStyle.SCIENTIFIC
)

enum class KeyVisualStyle {
    DIGIT,
    OPERATOR,
    SCIENTIFIC,
    ACCENT_SHIFT,
    DESTRUCTIVE,
    EQUALS,
    MEMORY
}

@Composable
fun MemoryAndDrawerHeaderBar(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    showDrawerToggle: Boolean = true,
    modifier: Modifier = Modifier
) {
    val memoryKeys = listOf(
        KeySpec("MC", "key_mc", CalculatorAction.MemoryClear, KeyVisualStyle.MEMORY),
        KeySpec("MR", "key_mr", CalculatorAction.MemoryRecall, KeyVisualStyle.MEMORY),
        KeySpec("M+", "key_m_plus", CalculatorAction.MemoryAdd, KeyVisualStyle.MEMORY),
        KeySpec("M−", "key_m_minus", CalculatorAction.MemorySubtract, KeyVisualStyle.MEMORY),
        KeySpec("MS", "key_ms", CalculatorAction.MemoryStore, KeyVisualStyle.MEMORY)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        memoryKeys.forEach { key ->
            val enabled = if (key.label == "MC" || key.label == "MR") uiState.hasMemory else true
            CalculatorKeyButton(
                label = key.label,
                testTag = key.testTag,
                style = key.style,
                enabled = enabled,
                fontSize = 12.sp,
                cornerRadius = 10.dp,
                onClick = { onAction(key.action) },
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            )
        }

        if (showDrawerToggle) {
            Surface(
                onClick = { onAction(CalculatorAction.ToggleScientificDrawer) },
                shape = RoundedCornerShape(10.dp),
                color = if (uiState.isScientificExpanded) ElectricCyanContainer else ObsidianSurfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (uiState.isScientificExpanded) ElectricCyan.copy(alpha = 0.5f) else ObsidianDisplayBorder
                ),
                modifier = Modifier
                    .height(38.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("toggle_scientific_drawer_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "SCI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (uiState.isScientificExpanded) ElectricCyan else TextSecondarySlate,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (uiState.isScientificExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.cd_toggle_scientific_drawer),
                        tint = if (uiState.isScientificExpanded) ElectricCyan else TextSecondarySlate,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableScientificMatrix(
    isExpanded: Boolean,
    isSecondActive: Boolean,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
    fillAvailableHeight: Boolean = false
) {
    val rows = remember(isSecondActive) {
        buildScientificRows(isSecondActive)
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = shrinkVertically(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = if (fillAvailableHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
        ) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = if (fillAvailableHeight) {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    }
                ) {
                    row.forEach { key ->
                        val activeShift = key.testTag == "key_second" && isSecondActive
                        CalculatorKeyButton(
                            label = key.label,
                            testTag = key.testTag,
                            style = if (activeShift) KeyVisualStyle.ACCENT_SHIFT else key.style,
                            fontSize = 13.sp,
                            cornerRadius = 12.dp,
                            onClick = { onAction(key.action) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryKeypadGrid(
    hasExpression: Boolean,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val clearLabel = if (hasExpression) stringResource(R.string.key_clear_entry) else stringResource(R.string.key_ac)
    val rows = listOf(
        listOf(
            KeySpec(clearLabel, "key_clear", CalculatorAction.ClearAll, KeyVisualStyle.DESTRUCTIVE),
            KeySpec("(", "key_open_paren", CalculatorAction.InputOpenParen, KeyVisualStyle.SCIENTIFIC),
            KeySpec(")", "key_close_paren", CalculatorAction.InputCloseParen, KeyVisualStyle.SCIENTIFIC),
            KeySpec("⌫", "key_backspace", CalculatorAction.Backspace, KeyVisualStyle.DESTRUCTIVE),
            KeySpec("÷", "key_divide", CalculatorAction.InputOperator("÷"), KeyVisualStyle.OPERATOR)
        ),
        listOf(
            KeySpec("7", "key_7", CalculatorAction.InputDigit("7"), KeyVisualStyle.DIGIT),
            KeySpec("8", "key_8", CalculatorAction.InputDigit("8"), KeyVisualStyle.DIGIT),
            KeySpec("9", "key_9", CalculatorAction.InputDigit("9"), KeyVisualStyle.DIGIT),
            KeySpec("%", "key_percent", CalculatorAction.InputPostfixToken("%"), KeyVisualStyle.SCIENTIFIC),
            KeySpec("×", "key_multiply", CalculatorAction.InputOperator("×"), KeyVisualStyle.OPERATOR)
        ),
        listOf(
            KeySpec("4", "key_4", CalculatorAction.InputDigit("4"), KeyVisualStyle.DIGIT),
            KeySpec("5", "key_5", CalculatorAction.InputDigit("5"), KeyVisualStyle.DIGIT),
            KeySpec("6", "key_6", CalculatorAction.InputDigit("6"), KeyVisualStyle.DIGIT),
            KeySpec("^", "key_power_quick", CalculatorAction.InputOperator("^"), KeyVisualStyle.SCIENTIFIC),
            KeySpec("−", "key_subtract", CalculatorAction.InputOperator("−"), KeyVisualStyle.OPERATOR)
        ),
        listOf(
            KeySpec("1", "key_1", CalculatorAction.InputDigit("1"), KeyVisualStyle.DIGIT),
            KeySpec("2", "key_2", CalculatorAction.InputDigit("2"), KeyVisualStyle.DIGIT),
            KeySpec("3", "key_3", CalculatorAction.InputDigit("3"), KeyVisualStyle.DIGIT),
            KeySpec("√", "key_sqrt_quick", CalculatorAction.InputPrefixFunction("√("), KeyVisualStyle.SCIENTIFIC),
            KeySpec("+", "key_add", CalculatorAction.InputOperator("+"), KeyVisualStyle.OPERATOR)
        ),
        listOf(
            KeySpec("±", "key_negate", CalculatorAction.ToggleSign, KeyVisualStyle.DIGIT),
            KeySpec("0", "key_0", CalculatorAction.InputDigit("0"), KeyVisualStyle.DIGIT),
            KeySpec(".", "key_decimal", CalculatorAction.InputDecimal, KeyVisualStyle.DIGIT),
            KeySpec("π", "key_pi_quick", CalculatorAction.InputConstant("π"), KeyVisualStyle.SCIENTIFIC),
            KeySpec("=", "key_equals", CalculatorAction.Evaluate, KeyVisualStyle.EQUALS)
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                row.forEach { key ->
                    val fontSize = when (key.style) {
                        KeyVisualStyle.DIGIT -> 22.sp
                        KeyVisualStyle.OPERATOR, KeyVisualStyle.EQUALS -> 24.sp
                        else -> 16.sp
                    }
                    CalculatorKeyButton(
                        label = key.label,
                        testTag = key.testTag,
                        style = key.style,
                        fontSize = fontSize,
                        cornerRadius = 16.dp,
                        onClick = { onAction(key.action) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorKeyButton(
    label: String,
    testTag: String,
    style: KeyVisualStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 18.sp,
    cornerRadius: Dp = 14.dp
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "key_press_scale"
    )

    val containerColor = when (style) {
        KeyVisualStyle.DIGIT -> ObsidianElevatedKey
        KeyVisualStyle.OPERATOR -> WarmAmberContainer
        KeyVisualStyle.SCIENTIFIC -> ObsidianScientificKey
        KeyVisualStyle.ACCENT_SHIFT -> WarmAmber
        KeyVisualStyle.DESTRUCTIVE -> CoralRedContainer
        KeyVisualStyle.EQUALS -> ElectricCyan
        KeyVisualStyle.MEMORY -> ObsidianSurfaceVariant
    }

    val contentColor = when (style) {
        KeyVisualStyle.DIGIT -> TextPrimaryWhite
        KeyVisualStyle.OPERATOR -> WarmAmber
        KeyVisualStyle.SCIENTIFIC -> ElectricCyan
        KeyVisualStyle.ACCENT_SHIFT -> Color(0xFF1A1100)
        KeyVisualStyle.DESTRUCTIVE -> CoralRed
        KeyVisualStyle.EQUALS -> OnElectricCyan
        KeyVisualStyle.MEMORY -> if (enabled) EmeraldMint else TextMutedSlate
    }

    val borderColor = when (style) {
        KeyVisualStyle.DIGIT -> Color(0xFF28354D)
        KeyVisualStyle.OPERATOR -> WarmAmber.copy(alpha = 0.4f)
        KeyVisualStyle.SCIENTIFIC -> ObsidianDisplayBorder
        KeyVisualStyle.ACCENT_SHIFT -> WarmAmber
        KeyVisualStyle.DESTRUCTIVE -> CoralRed.copy(alpha = 0.35f)
        KeyVisualStyle.EQUALS -> ElectricCyan
        KeyVisualStyle.MEMORY -> ObsidianDisplayBorder
    }

    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        shape = shape,
        color = if (style == KeyVisualStyle.EQUALS) Color.Transparent else containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .minimumInteractiveComponentSize()
            .clip(shape)
            .then(
                if (style == KeyVisualStyle.EQUALS) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(ElectricCyan, ElectricCyanDim)
                        ),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = fontSize,
                fontFamily = if (style == KeyVisualStyle.DIGIT || style == KeyVisualStyle.OPERATOR || style == KeyVisualStyle.EQUALS) {
                    JetBrainsMonoFontFamily
                } else {
                    SpaceGroteskFontFamily
                },
                fontWeight = if (style == KeyVisualStyle.DIGIT || style == KeyVisualStyle.EQUALS || style == KeyVisualStyle.OPERATOR) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun buildScientificRows(isSecond: Boolean): List<List<KeySpec>> {
    return listOf(
        listOf(
            KeySpec("2nd", "key_second", CalculatorAction.ToggleSecondFunction, KeyVisualStyle.SCIENTIFIC),
            if (!isSecond) {
                KeySpec("sin", "key_sin", CalculatorAction.InputPrefixFunction("sin("))
            } else {
                KeySpec("sin⁻¹", "key_asin", CalculatorAction.InputPrefixFunction("sin⁻¹("))
            },
            if (!isSecond) {
                KeySpec("cos", "key_cos", CalculatorAction.InputPrefixFunction("cos("))
            } else {
                KeySpec("cos⁻¹", "key_acos", CalculatorAction.InputPrefixFunction("cos⁻¹("))
            },
            if (!isSecond) {
                KeySpec("tan", "key_tan", CalculatorAction.InputPrefixFunction("tan("))
            } else {
                KeySpec("tan⁻¹", "key_atan", CalculatorAction.InputPrefixFunction("tan⁻¹("))
            },
            KeySpec("n!", "key_factorial", CalculatorAction.InputPostfixToken("!"))
        ),
        listOf(
            if (!isSecond) {
                KeySpec("sinh", "key_sinh", CalculatorAction.InputPrefixFunction("sinh("))
            } else {
                KeySpec("sinh⁻¹", "key_asinh", CalculatorAction.InputPrefixFunction("sinh⁻¹("))
            },
            if (!isSecond) {
                KeySpec("cosh", "key_cosh", CalculatorAction.InputPrefixFunction("cosh("))
            } else {
                KeySpec("cosh⁻¹", "key_acosh", CalculatorAction.InputPrefixFunction("cosh⁻¹("))
            },
            if (!isSecond) {
                KeySpec("tanh", "key_tanh", CalculatorAction.InputPrefixFunction("tanh("))
            } else {
                KeySpec("tanh⁻¹", "key_atanh", CalculatorAction.InputPrefixFunction("tanh⁻¹("))
            },
            if (!isSecond) {
                KeySpec("ln", "key_ln", CalculatorAction.InputPrefixFunction("ln("))
            } else {
                KeySpec("eˣ", "key_exp", CalculatorAction.InputPrefixFunction("e^("))
            },
            if (!isSecond) {
                KeySpec("log", "key_log10", CalculatorAction.InputPrefixFunction("log("))
            } else {
                KeySpec("10ˣ", "key_ten_pow", CalculatorAction.InputPrefixFunction("10^("))
            }
        ),
        listOf(
            if (!isSecond) {
                KeySpec("log₂", "key_log2", CalculatorAction.InputPrefixFunction("log₂("))
            } else {
                KeySpec("2ˣ", "key_two_pow", CalculatorAction.InputPrefixFunction("2^("))
            },
            if (!isSecond) {
                KeySpec("x²", "key_square", CalculatorAction.InputPostfixToken("²"))
            } else {
                KeySpec("x³", "key_cube", CalculatorAction.InputPostfixToken("³"))
            },
            if (!isSecond) {
                KeySpec("xʸ", "key_pow", CalculatorAction.InputOperator("^"))
            } else {
                KeySpec("ʸ√x", "key_yroot", CalculatorAction.InputOperator("ʸ√"))
            },
            if (!isSecond) {
                KeySpec("√x", "key_sqrt", CalculatorAction.InputPrefixFunction("√("))
            } else {
                KeySpec("∛x", "key_cbrt", CalculatorAction.InputPrefixFunction("∛("))
            },
            KeySpec("1/x", "key_reciprocal", CalculatorAction.InputPostfixToken("⁻¹"))
        ),
        listOf(
            KeySpec("|x|", "key_abs", CalculatorAction.InputPrefixFunction("abs(")),
            KeySpec("mod", "key_mod", CalculatorAction.InputOperator("mod")),
            if (!isSecond) {
                KeySpec("nPr", "key_npr", CalculatorAction.InputOperator("P"))
            } else {
                KeySpec("nCr", "key_ncr", CalculatorAction.InputOperator("C"))
            },
            KeySpec("e", "key_e", CalculatorAction.InputConstant("e")),
            KeySpec("Ans", "key_ans", CalculatorAction.InputConstant("Ans"))
        )
    )
}
