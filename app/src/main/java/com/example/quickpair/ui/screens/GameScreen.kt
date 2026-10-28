package com.example.quickpair.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickpair.model.Difficulty
import com.example.quickpair.model.GameState
import com.example.quickpair.model.PairTile
import com.example.quickpair.ui.components.CountdownOverlay
import com.example.quickpair.ui.components.ExitConfirmDialog
import com.example.quickpair.ui.components.PairTileView
import com.example.quickpair.ui.components.PauseDialog
import com.example.quickpair.ui.components.RestartConfirmDialog
import com.example.quickpair.ui.components.StreakBanner
import com.example.quickpair.ui.viewmodel.GameUiState

@Composable
fun GameScreen(
    state: GameUiState,
    reducedMotion: Boolean,
    onTileClicked: (PairTile) -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onRequestRestart: () -> Unit,
    onConfirmRestart: () -> Unit,
    onDismissRestart: () -> Unit,
    onRequestExit: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExit: () -> Unit
) {
    // Android Back Button during game: opens Pause dialog
    BackHandler {
        if (state.gameState == GameState.PLAYING) {
            onPauseClicked()
        } else if (state.showRestartConfirm) {
            onDismissRestart()
        } else if (state.showExitConfirm) {
            onDismissExit()
        } else if (state.showPauseDialog) {
            onResumeClicked()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                GameTopBar(
                    pairsRemaining = state.pairsRemaining,
                    score = state.score,
                    boardNumber = state.boardNumber,
                    streak = state.pairStreak,
                    onPause = onPauseClicked
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Timer Display
                GameTimerSection(
                    remainingTime = state.remainingTime,
                    maxTime = state.maxTime,
                    isWarning = state.isTimeWarning,
                    isUrgent = state.isTimeUrgent,
                    reducedMotion = reducedMotion
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Grid Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardGrid(
                        board = state.board,
                        difficulty = state.difficulty,
                        shakingTileIds = state.shakingTileIds,
                        matchedTileIds = state.matchedTileIds,
                        reducedMotion = reducedMotion,
                        onTileClicked = onTileClicked
                    )

                    // Floating feedback toast (MATCH! +100 +1.5s / NOT A PAIR / BOARD CLEAR!)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.feedbackMessage != null,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { -it / 2 }
                    ) {
                        state.feedbackMessage?.let { msg ->
                            val isNegative = msg.contains("NOT A PAIR")
                            val isBoardClear = msg.contains("BOARD CLEAR")

                            val bgColor = when {
                                isNegative -> MaterialTheme.colorScheme.errorContainer
                                isBoardClear -> Color(0xFF10B981)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }

                            val textColor = when {
                                isNegative -> MaterialTheme.colorScheme.onErrorContainer
                                isBoardClear -> Color.White
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = bgColor,
                                shadowElevation = 8.dp,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }

                    // Streak Banner milestone
                    StreakBanner(
                        title = state.streakBanner,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Countdown Overlay
        CountdownOverlay(text = state.countdownText)

        // Pause and confirmation dialogs
        if (state.showPauseDialog && !state.showRestartConfirm && !state.showExitConfirm) {
            PauseDialog(
                onResume = onResumeClicked,
                onRequestRestart = onRequestRestart,
                onRequestExit = onRequestExit
            )
        }

        if (state.showRestartConfirm) {
            RestartConfirmDialog(
                onConfirm = onConfirmRestart,
                onDismiss = onDismissRestart
            )
        }

        if (state.showExitConfirm) {
            ExitConfirmDialog(
                onConfirm = onConfirmExit,
                onDismiss = onDismissExit
            )
        }
    }
}

@Composable
private fun GameTopBar(
    pairsRemaining: Int,
    score: Int,
    boardNumber: Int,
    streak: Int,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pairs Remaining
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "PAIRS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "$pairsRemaining",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Streak info if active
        if (streak >= 2) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF97316).copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STREAK $streak 🔥",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF97316)
                    )
                )
            }
        } else {
            // Board indicator
            Text(
                text = "BOARD $boardNumber",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Score
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "SCORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Pause button: Ⅱ
        FilledIconButton(
            onClick = onPause,
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Ⅱ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GameTimerSection(
    remainingTime: Double,
    maxTime: Double,
    isWarning: Boolean,
    isUrgent: Boolean,
    reducedMotion: Boolean
) {
    val progress = (remainingTime / maxTime).toFloat().coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseScale by if ((isWarning || isUrgent) && !reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isUrgent) 1.12f else 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (isUrgent) 220 else 400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        rememberInfiniteTransition(label = "pulseStatic").animateFloat(
            initialValue = 1.0f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "pulseStaticVal"
        )
    }

    val timerColor = when {
        isUrgent -> MaterialTheme.colorScheme.error
        isWarning -> Color(0xFFF97316) // Warning Orange
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "TIME",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = timerColor
            )
        )

        Text(
            text = "${"%.1f".format(remainingTime)}s",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = timerColor
            ),
            modifier = Modifier.scale(pulseScale)
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun GameBoardGrid(
    board: List<PairTile>,
    difficulty: Difficulty,
    shakingTileIds: Set<Int>,
    matchedTileIds: Set<Int>,
    reducedMotion: Boolean,
    onTileClicked: (PairTile) -> Unit
) {
    if (board.isEmpty()) return

    // Choose grid columns based on tile count:
    // 12 tiles (Easy): 3 columns x 4 rows
    // 16 tiles (Medium): 4 columns x 4 rows
    // 20 or 24 tiles (Hard): 4 columns x 5 or 6 rows
    val columnsCount = when {
        board.size <= 12 -> 3
        else -> 4
    }

    val iconSize: Dp = when {
        board.size <= 12 -> 42.dp
        board.size <= 16 -> 36.dp
        else -> 28.dp
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            contentPadding = PaddingValues(2.dp),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {
            items(
                items = board,
                key = { it.id }
            ) { tile ->
                PairTileView(
                    tile = tile,
                    isShaking = shakingTileIds.contains(tile.id),
                    isMatching = matchedTileIds.contains(tile.id),
                    reducedMotion = reducedMotion,
                    iconSize = iconSize,
                    onClick = { onTileClicked(tile) }
                )
            }
        }
    }
}
