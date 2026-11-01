package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.CalculatorAction
import com.example.domain.CalculatorUiState
import com.example.ui.components.DisplayWellSection
import com.example.ui.components.ExpandableScientificMatrix
import com.example.ui.components.HistoryAndConstantsPane
import com.example.ui.components.MemoryAndDrawerHeaderBar
import com.example.ui.components.PrimaryKeypadGrid
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianDisplayBorder
import com.example.ui.theme.ObsidianSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.copied_to_clipboard)

    Scaffold(
        containerColor = ObsidianBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 680.dp
            val isLandscapeCompact = maxWidth > maxHeight && maxHeight < 520.dp

            if (isWideScreen) {
                ExpandedTwoPaneCalculatorLayout(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onShowCopiedToast = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(copiedMessage)
                        }
                    }
                )
            } else if (isLandscapeCompact) {
                LandscapeCompactCalculatorLayout(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onShowCopiedToast = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(copiedMessage)
                        }
                    }
                )
            } else {
                PortraitCalculatorLayout(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onShowCopiedToast = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(copiedMessage)
                        }
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (!isWideScreen && uiState.isHistorySheetOpen) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onAction(CalculatorAction.ToggleHistorySheet(open = false))
                    },
                    sheetState = sheetState,
                    containerColor = ObsidianSurface
                ) {
                    HistoryAndConstantsPane(
                        uiState = uiState,
                        onAction = viewModel::onAction,
                        isModal = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitCalculatorLayout(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    onShowCopiedToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 560.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DisplayWellSection(
            uiState = uiState,
            onAction = onAction,
            onShowCopiedToast = onShowCopiedToast,
            showHistoryTriggers = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (uiState.isScientificExpanded) 0.28f else 0.36f)
        )

        MemoryAndDrawerHeaderBar(
            uiState = uiState,
            onAction = onAction,
            showDrawerToggle = true
        )

        ExpandableScientificMatrix(
            isExpanded = uiState.isScientificExpanded,
            isSecondActive = uiState.isSecondFunctionActive,
            onAction = onAction
        )

        PrimaryKeypadGrid(
            hasExpression = uiState.expression.isNotEmpty(),
            onAction = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
        )
    }
}

@Composable
private fun LandscapeCompactCalculatorLayout(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    onShowCopiedToast: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DisplayWellSection(
            uiState = uiState,
            onAction = onAction,
            onShowCopiedToast = onShowCopiedToast,
            showHistoryTriggers = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.34f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.66f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MemoryAndDrawerHeaderBar(
                    uiState = uiState,
                    onAction = onAction,
                    showDrawerToggle = false
                )
                ExpandableScientificMatrix(
                    isExpanded = true,
                    isSecondActive = uiState.isSecondFunctionActive,
                    onAction = onAction,
                    fillAvailableHeight = true,
                    modifier = Modifier.weight(1f)
                )
            }

            PrimaryKeypadGrid(
                hasExpression = uiState.expression.isNotEmpty(),
                onAction = onAction,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ExpandedTwoPaneCalculatorLayout(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    onShowCopiedToast: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Pane: Calculator Display + Scientific & Primary Keypads
        Column(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DisplayWellSection(
                uiState = uiState,
                onAction = onAction,
                onShowCopiedToast = onShowCopiedToast,
                showHistoryTriggers = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.30f)
            )

            MemoryAndDrawerHeaderBar(
                uiState = uiState,
                onAction = onAction,
                showDrawerToggle = true
            )

            ExpandableScientificMatrix(
                isExpanded = uiState.isScientificExpanded,
                isSecondActive = uiState.isSecondFunctionActive,
                onAction = onAction
            )

            PrimaryKeypadGrid(
                hasExpression = uiState.expression.isNotEmpty(),
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
            )
        }

        // Right Supporting Pane: Permanent History Tape, Constants & Formulas
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, ObsidianDisplayBorder),
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
        ) {
            HistoryAndConstantsPane(
                uiState = uiState,
                onAction = onAction,
                isModal = false
            )
        }
    }
}
