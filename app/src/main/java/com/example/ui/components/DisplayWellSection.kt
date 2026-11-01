package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.CalculatorAction
import com.example.domain.CalculatorUiState
import com.example.domain.MathEngine
import com.example.domain.NotationMode
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldMintContainer
import com.example.ui.theme.GhostParenColor
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianDisplayBorder
import com.example.ui.theme.ObsidianDisplayWell
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayWellSection(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    onShowCopiedToast: () -> Unit,
    showHistoryTriggers: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    val expressionScrollState = rememberScrollState()

    // Auto-scroll to the end of the expression as the user types
    LaunchedEffect(uiState.expression, uiState.ghostClosingParens) {
        expressionScrollState.animateScrollTo(expressionScrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ObsidianDisplayWell)
            .drawBehind {
                // Subtle dual-glow atmospheric background inside the OLED display well
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricCyan.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.2f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(WarmAmber.copy(alpha = 0.05f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.85f),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.15f, size.height * 0.85f)
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = 0.32f),
                        ObsidianDisplayBorder,
                        WarmAmber.copy(alpha = 0.18f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Status & Mode Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                ModePillButton(
                    text = uiState.angleMode.label,
                    accentColor = ElectricCyan,
                    containerColor = ElectricCyanContainer,
                    contentDescription = stringResource(R.string.cd_toggle_angle_mode),
                    testTag = "angle_mode_button",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAction(CalculatorAction.ToggleAngleMode)
                    }
                )

                ModePillButton(
                    text = uiState.notationMode.label,
                    accentColor = WarmAmber,
                    containerColor = WarmAmberContainer,
                    contentDescription = stringResource(R.string.cd_toggle_notation_mode),
                    testTag = "notation_mode_button",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAction(CalculatorAction.ToggleNotationMode)
                    }
                )

                if (uiState.isSecondFunctionActive) {
                    StatusBadge(
                        text = stringResource(R.string.second_shift_active),
                        textColor = WarmAmber,
                        containerColor = WarmAmberContainer
                    )
                }

                if (uiState.hasMemory) {
                    val formattedMem = MathEngine.formatNumber(uiState.memoryValue, NotationMode.NORMAL)
                    StatusBadge(
                        text = stringResource(R.string.memory_indicator_prefix, formattedMem),
                        textColor = EmeraldMint,
                        containerColor = EmeraldMintContainer
                    )
                }
            }

            if (showHistoryTriggers) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAction(CalculatorAction.ToggleHistorySheet(open = true, initialTab = 1))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("constants_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Science,
                            contentDescription = stringResource(R.string.tab_constants),
                            tint = EmeraldMint
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAction(CalculatorAction.ToggleHistorySheet(open = true, initialTab = 0))
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("history_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(R.string.cd_open_history),
                                tint = ElectricCyan
                            )
                            if (uiState.history.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(WarmAmber)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Expression Readout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(expressionScrollState)
                .testTag("expression_display"),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (uiState.expression.isEmpty()) {
                Text(
                    text = stringResource(R.string.display_placeholder),
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = TextMutedSlate
                    ),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            } else {
                val styledExpression = buildSyntaxHighlightedExpression(
                    expression = uiState.expression,
                    ghostParens = uiState.ghostClosingParens
                )
                val dynamicFontSize = when {
                    uiState.expression.length > 24 -> 26.sp
                    uiState.expression.length > 15 -> 32.sp
                    else -> 42.sp
                }
                Text(
                    text = styledExpression,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = dynamicFontSize,
                        lineHeight = (dynamicFontSize.value + 8).sp
                    ),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Live Result Preview & Last Answer Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Answer Quick Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAction(CalculatorAction.InputConstant("Ans"))
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = TextMutedSlate,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.ans_indicator_prefix, uiState.lastAnswerFormatted),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondarySlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Live Evaluated Result or Domain Error
            AnimatedContent(
                targetState = Pair(uiState.errorMessage, uiState.livePreview),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "live_preview_anim"
            ) { (errorMsg, previewText) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = CoralRed
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("error_display")
                        )
                    } else if (previewText.isNotEmpty()) {
                        Text(
                            text = "= $previewText",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = ElectricCyan,
                                fontSize = 22.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("live_preview_display")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(previewText))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onShowCopiedToast()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("copy_result_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.cd_copy_result),
                                tint = TextSecondarySlate,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePillButton(
    text: String,
    accentColor: Color,
    containerColor: Color,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .semantics { this.contentDescription = contentDescription }
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    textColor: Color,
    containerColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

private fun buildSyntaxHighlightedExpression(
    expression: String,
    ghostParens: String
): AnnotatedString = buildAnnotatedString {
    val operators = setOf('+', '−', '-', '×', '÷', '^', '%', '!')
    val constants = setOf('π', 'e', 'φ')
    for (ch in expression) {
        when {
            ch in operators -> {
                withStyle(SpanStyle(color = WarmAmber, fontWeight = FontWeight.Bold)) {
                    append(ch)
                }
            }
            ch == '(' || ch == ')' -> {
                withStyle(SpanStyle(color = ElectricCyan, fontWeight = FontWeight.SemiBold)) {
                    append(ch)
                }
            }
            ch in constants || ch.isLetter() || ch == '√' || ch == '∛' -> {
                withStyle(SpanStyle(color = ElectricCyan)) {
                    append(ch)
                }
            }
            else -> {
                withStyle(SpanStyle(color = TextPrimaryWhite)) {
                    append(ch)
                }
            }
        }
    }
    if (ghostParens.isNotEmpty()) {
        withStyle(SpanStyle(color = GhostParenColor)) {
            append(ghostParens)
        }
    }
}
