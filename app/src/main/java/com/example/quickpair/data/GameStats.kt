package com.example.quickpair.data

data class GameStats(
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val bestPairs: Int = 0,
    val bestAccuracy: Double = 0.0,
    val bestMatchStreak: Int = 0,
    val bestBoardsCleared: Int = 0,
    val totalPairsMatched: Int = 0,
    val totalWrongAttempts: Int = 0
)
