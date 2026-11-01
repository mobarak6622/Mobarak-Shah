package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.CalculationRecord
import com.example.domain.CalculatorAction
import com.example.domain.CalculatorUiState
import com.example.domain.PhysicalConstant
import com.example.domain.SampleFormula
import com.example.domain.ScientificCatalog
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldMintContainer
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianDisplayBorder
import com.example.ui.theme.ObsidianDisplayWell
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextMutedSlate
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAndConstantsPane(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    isModal: Boolean,
    modifier: Modifier = Modifier
) {
    if (isModal && uiState.isHistorySheetOpen) {
        BackHandler {
            onAction(CalculatorAction.ToggleHistorySheet(open = false))
        }
    }

    val tabs = listOf(
        stringResource(R.string.tab_history),
        stringResource(R.string.tab_constants),
        stringResource(R.string.tab_formulas)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tabs[uiState.selectedSheetTab],
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.selectedSheetTab == 0 && uiState.history.any { !it.isPinned }) {
                    TextButton(
                        onClick = { onAction(CalculatorAction.ClearUnpinnedHistory) },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Text(
                            text = stringResource(R.string.action_clear_history),
                            color = CoralRed,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                if (isModal) {
                    IconButton(
                        onClick = { onAction(CalculatorAction.ToggleHistorySheet(open = false)) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close_sheet),
                            tint = TextSecondarySlate
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Row
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedSheetTab,
            containerColor = ObsidianDisplayWell,
            contentColor = ElectricCyan,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedSheetTab == index,
                    onClick = {
                        onAction(
                            CalculatorAction.ToggleHistorySheet(
                                open = uiState.isHistorySheetOpen,
                                initialTab = index
                            )
                        )
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selectedContentColor = ElectricCyan,
                    unselectedContentColor = TextSecondarySlate,
                    modifier = Modifier.testTag("sheet_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (uiState.selectedSheetTab) {
            0 -> HistoryTapeList(
                history = uiState.history,
                onAction = onAction,
                modifier = Modifier.weight(1f)
            )
            1 -> ConstantsCatalogList(
                constants = ScientificCatalog.constants,
                onAction = onAction,
                modifier = Modifier.weight(1f)
            )
            2 -> SampleFormulasList(
                formulas = ScientificCatalog.sampleFormulas,
                onAction = onAction,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HistoryTapeList(
    history: List<CalculationRecord>,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianDisplayWell,
                    border = BorderStroke(1.dp, ObsidianDisplayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_scientific_hero),
                            contentDescription = null,
                            modifier = Modifier
                                .height(84.dp)
                                .fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.history_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.history_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(ScientificCatalog.sampleFormulas.take(3)) { sample ->
                SampleFormulaCard(
                    formula = sample,
                    onSelect = { onAction(CalculatorAction.LoadExpression(sample.expression)) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(history, key = { it.id }) { record ->
                HistoryRecordCard(record = record, onAction = onAction)
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(
    record: CalculationRecord,
    onAction: (CalculatorAction) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (record.isPinned) WarmAmber.copy(alpha = 0.5f) else ObsidianDisplayBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricCyanContainer
                    ) {
                        Text(
                            text = record.angleMode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = record.timestampLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedSlate
                    )
                }

                Row {
                    IconButton(
                        onClick = { onAction(CalculatorAction.TogglePinHistory(record.id)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (record.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.cd_pin_calculation),
                            tint = if (record.isPinned) WarmAmber else TextSecondarySlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onAction(CalculatorAction.DeleteHistoryItem(record.id)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_calculation),
                            tint = TextSecondarySlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = record.expression,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    color = TextSecondarySlate
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "= ${record.result}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    color = ElectricCyan
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = { onAction(CalculatorAction.LoadExpression(record.expression)) },
                    border = BorderStroke(1.dp, ObsidianDisplayBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_use_expression),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimaryWhite
                    )
                }
                FilledTonalButton(
                    onClick = { onAction(CalculatorAction.InsertHistoryResult(record.result)) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ElectricCyanContainer,
                        contentColor = ElectricCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_use_result),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ConstantsCatalogList(
    constants: List<PhysicalConstant>,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(constants, key = { it.symbol }) { constant ->
            Surface(
                onClick = {
                    onAction(CalculatorAction.InputConstant(constant.expressionToken))
                    onAction(CalculatorAction.ToggleHistorySheet(open = false))
                },
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceVariant,
                border = BorderStroke(1.dp, ObsidianDisplayBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("constant_item_${constant.symbol}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldMintContainer)
                        ) {
                            Text(
                                text = constant.symbol,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = constant.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryWhite
                                )
                            )
                            Text(
                                text = if (constant.unit.isNotEmpty()) {
                                    "${constant.displayValue} ${constant.unit}"
                                } else {
                                    constant.displayValue
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondarySlate
                                )
                            )
                        }
                    }

                    Text(
                        text = "INSERT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleFormulasList(
    formulas: List<SampleFormula>,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(formulas, key = { it.title }) { formula ->
            SampleFormulaCard(
                formula = formula,
                onSelect = { onAction(CalculatorAction.LoadExpression(formula.expression)) }
            )
        }
    }
}

@Composable
private fun SampleFormulaCard(
    formula: SampleFormula,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = ObsidianSurfaceVariant,
        border = BorderStroke(1.dp, ObsidianDisplayBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formula.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryWhite
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WarmAmberContainer
                ) {
                    Text(
                        text = formula.category,
                        style = MaterialTheme.typography.labelSmall.copy(color = WarmAmber),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formula.expression,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    color = ElectricCyan
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formula.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondarySlate)
            )
        }
    }
}
