package com.example.quickpair.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickpair.data.GamePreferences
import com.example.quickpair.data.GameStats
import com.example.quickpair.data.SoundManager
import com.example.quickpair.data.ThemeMode
import com.example.quickpair.model.Difficulty
import com.example.quickpair.model.GameOverReason
import com.example.quickpair.model.GameState
import com.example.quickpair.model.PairTile
import com.example.quickpair.model.QuickPairEngine
import com.example.quickpair.model.QuickPairResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val gameState: GameState = GameState.READY,
    val difficulty: Difficulty = Difficulty.EASY,
    val board: List<PairTile> = emptyList(),
    val selectedTile: PairTile? = null,
    val remainingTime: Double = 20.0,
    val maxTime: Double = 25.0,
    val score: Int = 0,
    val pairsMatched: Int = 0,
    val wrongAttempts: Int = 0,
    val pairStreak: Int = 0,
    val bestStreak: Int = 0,
    val boardNumber: Int = 1,
    val countdownText: String = "",
    val feedbackMessage: String? = null,
    val streakBanner: String? = null,
    val shakingTileIds: Set<Int> = emptySet(),
    val matchedTileIds: Set<Int> = emptySet(),
    val connectorTileIds: Pair<Int, Int>? = null,
    val showPauseDialog: Boolean = false,
    val showRestartConfirm: Boolean = false,
    val showExitConfirm: Boolean = false,
    val isTimeWarning: Boolean = false,
    val isTimeUrgent: Boolean = false
) {
    val pairsRemaining: Int
        get() = board.count { !it.matched } / 2

    val accuracy: Double
        get() = QuickPairEngine.calculateAccuracy(pairsMatched, wrongAttempts)
}

sealed interface GameNavigationEvent {
    object NavigateToResult : GameNavigationEvent
    object NavigateToHome : GameNavigationEvent
}

class QuickPairViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = GamePreferences(application.applicationContext)
    val soundManager = SoundManager(application.applicationContext)

    val stats: StateFlow<GameStats> = preferences.stats
    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
    val soundEnabled: StateFlow<Boolean> = preferences.soundEnabled
    val hapticsEnabled: StateFlow<Boolean> = preferences.hapticsEnabled
    val reducedMotion: StateFlow<Boolean> = preferences.reducedMotion

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<GameNavigationEvent>()
    val navEvents: SharedFlow<GameNavigationEvent> = _navEvents.asSharedFlow()

    private val _lastResult = MutableStateFlow<QuickPairResult?>(null)
    val lastResult: StateFlow<QuickPairResult?> = _lastResult.asStateFlow()

    private var timerJob: Job? = null
    private var countdownJob: Job? = null
    private var lastTickedSecond: Int = -1

    fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
    fun setSoundEnabled(enabled: Boolean) = preferences.setSoundEnabled(enabled)
    fun setHapticsEnabled(enabled: Boolean) = preferences.setHapticsEnabled(enabled)
    fun setReducedMotion(enabled: Boolean) = preferences.setReducedMotion(enabled)
    fun resetStatistics() = preferences.resetStatistics()

    fun startGame(difficulty: Difficulty) {
        timerJob?.cancel()
        countdownJob?.cancel()

        val pairCount = difficulty.startingPairs
        val initialBoard = QuickPairEngine.generateBoard(pairCount)

        _uiState.value = GameUiState(
            gameState = GameState.COUNTDOWN,
            difficulty = difficulty,
            board = initialBoard,
            selectedTile = null,
            remainingTime = difficulty.startingTimeSeconds,
            maxTime = difficulty.maxTimeSeconds,
            score = 0,
            pairsMatched = 0,
            wrongAttempts = 0,
            pairStreak = 0,
            bestStreak = 0,
            boardNumber = 1,
            countdownText = "3"
        )
        lastTickedSecond = -1

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob = viewModelScope.launch {
            val countdownSteps = listOf("3", "2", "1", "GO!")
            for (step in countdownSteps) {
                _uiState.update { it.copy(countdownText = step) }
                if (step == "GO!") {
                    soundManager.playCountdownGo(soundEnabled.value)
                    soundManager.vibrateSuccess(hapticsEnabled.value)
                    delay(500)
                } else {
                    soundManager.playCountdownTick(soundEnabled.value)
                    soundManager.vibrateSelection(hapticsEnabled.value)
                    delay(800)
                }
            }
            _uiState.update {
                it.copy(
                    gameState = GameState.PLAYING,
                    countdownText = ""
                )
            }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val tickIntervalMs = 50L
            while (_uiState.value.gameState == GameState.PLAYING) {
                delay(tickIntervalMs)
                val current = _uiState.value
                val newTime = current.remainingTime - (tickIntervalMs / 1000.0)

                val clamped = QuickPairEngine.clampTime(newTime, current.maxTime)
                val isWarning = clamped in 0.01..5.0
                val isUrgent = clamped in 0.01..2.0

                val currentSec = clamped.toInt()
                if (isWarning && currentSec != lastTickedSecond) {
                    lastTickedSecond = currentSec
                    soundManager.playTimeWarning(soundEnabled.value)
                }

                if (clamped <= 0.0) {
                    _uiState.update {
                        it.copy(
                            remainingTime = 0.0,
                            isTimeWarning = false,
                            isTimeUrgent = false
                        )
                    }
                    onGameOver(GameOverReason.TIMEOUT)
                    break
                } else {
                    _uiState.update {
                        it.copy(
                            remainingTime = clamped,
                            isTimeWarning = isWarning,
                            isTimeUrgent = isUrgent
                        )
                    }
                }
            }
        }
    }

    fun onTileClicked(tile: PairTile) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return
        if (tile.matched) return

        val firstSelected = state.selectedTile

        // Tap first tile or tap same tile
        if (firstSelected == null) {
            // Select first tile
            soundManager.playTileSelect(soundEnabled.value)
            soundManager.vibrateSelection(hapticsEnabled.value)
            _uiState.update { current ->
                current.copy(
                    selectedTile = tile,
                    board = current.board.map {
                        if (it.id == tile.id) it.copy(selected = true) else it.copy(selected = false)
                    }
                )
            }
            return
        }

        // Tap same tile -> Deselect it, no penalty
        if (firstSelected.id == tile.id) {
            soundManager.playTileSelect(soundEnabled.value)
            _uiState.update { current ->
                current.copy(
                    selectedTile = null,
                    board = current.board.map {
                        if (it.id == tile.id) it.copy(selected = false) else it
                    }
                )
            }
            return
        }

        // Tap second tile
        val isMatching = QuickPairEngine.isMatch(firstSelected, tile)
        if (isMatching) {
            handleCorrectMatch(firstSelected, tile)
        } else {
            handleWrongMatch(firstSelected, tile)
        }
    }

    private fun handleCorrectMatch(first: PairTile, second: PairTile) {
        soundManager.playMatchSuccess(soundEnabled.value)
        soundManager.vibrateSuccess(hapticsEnabled.value)

        val state = _uiState.value
        val newCorrectMatches = state.pairsMatched + 1
        val newStreak = state.pairStreak + 1
        val newBestStreak = maxOf(state.bestStreak, newStreak)

        val matchScore = QuickPairEngine.calculateMatchScore(state.boardNumber)
        val speedBonus = QuickPairEngine.calculateSpeedBonus(state.remainingTime)
        val streakBonus = QuickPairEngine.calculateStreakBonus(newStreak)
        val addedScore = matchScore + speedBonus + streakBonus

        val timeBonus = state.difficulty.matchBonusSeconds
        val updatedTime = QuickPairEngine.clampTime(state.remainingTime + timeBonus, state.maxTime)

        val milestone = QuickPairEngine.getStreakMilestone(newStreak)
        if (milestone != null) {
            soundManager.playStreakMilestone(soundEnabled.value)
        }

        val feedback = "MATCH!\n+$addedScore\n+${"%.1f".format(timeBonus)}s"

        val updatedBoard = state.board.map {
            if (it.id == first.id || it.id == second.id) {
                it.copy(matched = true, selected = false)
            } else {
                it
            }
        }

        _uiState.update { current ->
            current.copy(
                score = current.score + addedScore,
                pairsMatched = newCorrectMatches,
                pairStreak = newStreak,
                bestStreak = newBestStreak,
                remainingTime = updatedTime,
                selectedTile = null,
                board = updatedBoard,
                feedbackMessage = feedback,
                streakBanner = milestone,
                matchedTileIds = setOf(first.id, second.id),
                connectorTileIds = Pair(first.id, second.id)
            )
        }

        // Clear transient visual effects after delay
        viewModelScope.launch {
            delay(350)
            _uiState.update {
                it.copy(
                    matchedTileIds = emptySet(),
                    connectorTileIds = null,
                    feedbackMessage = null
                )
            }
        }

        if (milestone != null) {
            viewModelScope.launch {
                delay(1200)
                _uiState.update { it.copy(streakBanner = null) }
            }
        }

        // Check if all pairs on the board are cleared
        val remainingUnmatched = updatedBoard.count { !it.matched }
        if (remainingUnmatched == 0) {
            handleBoardClear()
        }
    }

    private fun handleBoardClear() {
        soundManager.playBoardClear(soundEnabled.value)
        soundManager.vibrateSuccess(hapticsEnabled.value)

        val state = _uiState.value
        val boardBonusScore = 250
        val boardBonusTime = state.difficulty.boardClearBonusTimeSeconds
        val updatedTime = QuickPairEngine.clampTime(state.remainingTime + boardBonusTime, state.maxTime)
        val nextBoardNumber = state.boardNumber + 1

        val clearFeedback = "BOARD CLEAR!\n+$boardBonusScore\n+${"%.1f".format(boardBonusTime)}s"

        _uiState.update { current ->
            current.copy(
                gameState = GameState.BOARD_CLEAR,
                score = current.score + boardBonusScore,
                remainingTime = updatedTime,
                feedbackMessage = clearFeedback
            )
        }

        viewModelScope.launch {
            delay(800)
            val nextPairCount = QuickPairEngine.getPairCountForBoard(
                state.difficulty.startingPairs,
                nextBoardNumber
            )
            val newBoard = QuickPairEngine.generateBoard(
                pairCount = nextPairCount,
                previousTiles = state.board
            )

            _uiState.update { current ->
                current.copy(
                    gameState = GameState.PLAYING,
                    boardNumber = nextBoardNumber,
                    board = newBoard,
                    selectedTile = null,
                    feedbackMessage = null
                )
            }
        }
    }

    private fun handleWrongMatch(first: PairTile, second: PairTile) {
        soundManager.playMatchWrong(soundEnabled.value)
        soundManager.vibrateWarning(hapticsEnabled.value)

        val state = _uiState.value
        val penalty = state.difficulty.wrongPenaltySeconds
        val updatedTime = maxOf(0.0, state.remainingTime - penalty)

        _uiState.update { current ->
            current.copy(
                wrongAttempts = current.wrongAttempts + 1,
                pairStreak = 0,
                remainingTime = updatedTime,
                selectedTile = null,
                feedbackMessage = "NOT A PAIR\n-${"%.1f".format(penalty)}s",
                shakingTileIds = setOf(first.id, second.id),
                board = current.board.map { it.copy(selected = false) }
            )
        }

        viewModelScope.launch {
            delay(300)
            _uiState.update {
                it.copy(
                    shakingTileIds = emptySet(),
                    feedbackMessage = null
                )
            }
        }

        if (updatedTime <= 0.0) {
            onGameOver(GameOverReason.TIMEOUT)
        }
    }

    private fun onGameOver(reason: GameOverReason) {
        timerJob?.cancel()
        countdownJob?.cancel()

        soundManager.playGameOver(soundEnabled.value)
        soundManager.vibrateGameOver(hapticsEnabled.value)

        val state = _uiState.value
        val accuracy = state.accuracy
        val boardsCleared = maxOf(0, state.boardNumber - 1)

        val (isNewScore, isNewPairs, isNewStreak) = preferences.recordGame(
            score = state.score,
            pairsMatched = state.pairsMatched,
            wrongAttempts = state.wrongAttempts,
            accuracy = accuracy,
            bestStreak = state.bestStreak,
            boardsCleared = boardsCleared
        )

        val result = QuickPairResult(
            difficulty = state.difficulty,
            score = state.score,
            pairsMatched = state.pairsMatched,
            wrongAttempts = state.wrongAttempts,
            accuracy = accuracy,
            bestStreak = state.bestStreak,
            boardsCleared = boardsCleared,
            reason = reason,
            pairsRemainingOnBoard = state.pairsRemaining,
            isNewBestScore = isNewScore,
            isNewBestPairs = isNewPairs,
            isNewBestStreak = isNewStreak
        )

        _lastResult.value = result
        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                selectedTile = null,
                feedbackMessage = null
            )
        }

        viewModelScope.launch {
            delay(400)
            _navEvents.emit(GameNavigationEvent.NavigateToResult)
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            timerJob?.cancel()
            _uiState.update {
                it.copy(
                    gameState = GameState.PAUSED,
                    showPauseDialog = true
                )
            }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update {
                it.copy(
                    gameState = GameState.PLAYING,
                    showPauseDialog = false,
                    showRestartConfirm = false,
                    showExitConfirm = false
                )
            }
            startTimer()
        }
    }

    fun requestRestart() {
        _uiState.update { it.copy(showRestartConfirm = true) }
    }

    fun dismissRestartConfirm() {
        _uiState.update { it.copy(showRestartConfirm = false) }
    }

    fun confirmRestart() {
        val difficulty = _uiState.value.difficulty
        _uiState.update {
            it.copy(
                showRestartConfirm = false,
                showPauseDialog = false
            )
        }
        startGame(difficulty)
    }

    fun requestExit() {
        _uiState.update { it.copy(showExitConfirm = true) }
    }

    fun dismissExitConfirm() {
        _uiState.update { it.copy(showExitConfirm = false) }
    }

    fun confirmExit() {
        timerJob?.cancel()
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                gameState = GameState.READY,
                showExitConfirm = false,
                showPauseDialog = false
            )
        }
        viewModelScope.launch {
            _navEvents.emit(GameNavigationEvent.NavigateToHome)
        }
    }

    fun playAgain() {
        val currentDiff = _lastResult.value?.difficulty ?: _uiState.value.difficulty
        startGame(currentDiff)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        countdownJob?.cancel()
    }
}
