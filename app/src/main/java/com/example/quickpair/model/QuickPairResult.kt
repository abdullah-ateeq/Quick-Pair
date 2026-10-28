package com.example.quickpair.model

data class QuickPairResult(
    val difficulty: Difficulty,
    val score: Int,
    val pairsMatched: Int,
    val wrongAttempts: Int,
    val accuracy: Double,
    val bestStreak: Int,
    val boardsCleared: Int,
    val reason: GameOverReason,
    val pairsRemainingOnBoard: Int = 0,
    val isNewBestScore: Boolean = false,
    val isNewBestPairs: Boolean = false,
    val isNewBestStreak: Boolean = false
)
