package com.example.quickpair.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS, true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _reducedMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCED_MOTION, false))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    private fun loadStats(): GameStats {
        return GameStats(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            bestPairs = prefs.getInt(KEY_BEST_PAIRS, 0),
            bestAccuracy = prefs.getFloat(KEY_BEST_ACCURACY, 0f).toDouble(),
            bestMatchStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            bestBoardsCleared = prefs.getInt(KEY_BEST_BOARDS_CLEARED, 0),
            totalPairsMatched = prefs.getInt(KEY_TOTAL_PAIRS_MATCHED, 0),
            totalWrongAttempts = prefs.getInt(KEY_TOTAL_WRONG_ATTEMPTS, 0)
        )
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _reducedMotion.value = enabled
    }

    fun recordGame(
        score: Int,
        pairsMatched: Int,
        wrongAttempts: Int,
        accuracy: Double,
        bestStreak: Int,
        boardsCleared: Int
    ): Triple<Boolean, Boolean, Boolean> {
        val current = _stats.value
        val isNewBestScore = score > current.bestScore
        val isNewBestPairs = pairsMatched > current.bestPairs
        val isNewBestStreak = bestStreak > current.bestMatchStreak

        val newBestScore = maxOf(score, current.bestScore)
        val newBestPairs = maxOf(pairsMatched, current.bestPairs)
        val newBestStreak = maxOf(bestStreak, current.bestMatchStreak)
        val newBestBoards = maxOf(boardsCleared, current.bestBoardsCleared)
        val newBestAccuracy = if (pairsMatched + wrongAttempts > 0) {
            maxOf(accuracy, current.bestAccuracy)
        } else {
            current.bestAccuracy
        }

        val updated = GameStats(
            gamesPlayed = current.gamesPlayed + 1,
            bestScore = newBestScore,
            bestPairs = newBestPairs,
            bestAccuracy = newBestAccuracy,
            bestMatchStreak = newBestStreak,
            bestBoardsCleared = newBestBoards,
            totalPairsMatched = current.totalPairsMatched + pairsMatched,
            totalWrongAttempts = current.totalWrongAttempts + wrongAttempts
        )

        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, updated.gamesPlayed)
            .putInt(KEY_BEST_SCORE, updated.bestScore)
            .putInt(KEY_BEST_PAIRS, updated.bestPairs)
            .putFloat(KEY_BEST_ACCURACY, updated.bestAccuracy.toFloat())
            .putInt(KEY_BEST_STREAK, updated.bestMatchStreak)
            .putInt(KEY_BEST_BOARDS_CLEARED, updated.bestBoardsCleared)
            .putInt(KEY_TOTAL_PAIRS_MATCHED, updated.totalPairsMatched)
            .putInt(KEY_TOTAL_WRONG_ATTEMPTS, updated.totalWrongAttempts)
            .apply()

        _stats.value = updated
        return Triple(isNewBestScore, isNewBestPairs, isNewBestStreak)
    }

    fun resetStatistics() {
        val reset = GameStats(
            gamesPlayed = 0,
            bestScore = 0,
            bestPairs = 0,
            bestAccuracy = 0.0,
            bestMatchStreak = 0,
            bestBoardsCleared = 0,
            totalPairsMatched = 0,
            totalWrongAttempts = 0
        )
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_BEST_PAIRS, 0)
            .putFloat(KEY_BEST_ACCURACY, 0f)
            .putInt(KEY_BEST_STREAK, 0)
            .putInt(KEY_BEST_BOARDS_CLEARED, 0)
            .putInt(KEY_TOTAL_PAIRS_MATCHED, 0)
            .putInt(KEY_TOTAL_WRONG_ATTEMPTS, 0)
            .apply()
        _stats.value = reset
    }

    companion object {
        private const val PREFS_NAME = "quick_pair_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_REDUCED_MOTION = "reduced_motion"

        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_BEST_PAIRS = "best_pairs"
        private const val KEY_BEST_ACCURACY = "best_accuracy"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_BEST_BOARDS_CLEARED = "best_boards_cleared"
        private const val KEY_TOTAL_PAIRS_MATCHED = "total_pairs_matched"
        private const val KEY_TOTAL_WRONG_ATTEMPTS = "total_wrong_attempts"
    }
}
