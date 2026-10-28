package com.example.quickpair.model

enum class Difficulty(
    val displayName: String,
    val startingPairs: Int,
    val startingTimeSeconds: Double,
    val matchBonusSeconds: Double,
    val wrongPenaltySeconds: Double,
    val maxTimeSeconds: Double,
    val boardClearBonusTimeSeconds: Double
) {
    EASY(
        displayName = "Easy",
        startingPairs = 6,
        startingTimeSeconds = 20.0,
        matchBonusSeconds = 1.5,
        wrongPenaltySeconds = 0.5,
        maxTimeSeconds = 25.0,
        boardClearBonusTimeSeconds = 2.0
    ),
    MEDIUM(
        displayName = "Medium",
        startingPairs = 8,
        startingTimeSeconds = 16.0,
        matchBonusSeconds = 1.2,
        wrongPenaltySeconds = 0.7,
        maxTimeSeconds = 20.0,
        boardClearBonusTimeSeconds = 2.0
    ),
    HARD(
        displayName = "Hard",
        startingPairs = 12,
        startingTimeSeconds = 13.0,
        matchBonusSeconds = 0.9,
        wrongPenaltySeconds = 1.0,
        maxTimeSeconds = 17.0,
        boardClearBonusTimeSeconds = 2.0
    )
}
