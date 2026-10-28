package com.example.quickpair.model

import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

object QuickPairEngine {

    /**
     * Generates a board of tiles where:
     * 1. Every symbol appears exactly twice.
     * 2. Total tile count = pairCount * 2.
     * 3. Positions are shuffled.
     * 4. Does not repeat the exact previous arrangement if previous provided.
     * 5. Tile IDs are unique (0 until pairCount * 2).
     */
    fun generateBoard(
        pairCount: Int,
        availableSymbols: List<PairSymbol> = PairSymbol.entries,
        previousTiles: List<PairTile>? = null,
        random: Random = Random.Default
    ): List<PairTile> {
        val totalTiles = pairCount * 2
        require(pairCount > 0) { "pairCount must be > 0" }
        require(availableSymbols.isNotEmpty()) { "availableSymbols must not be empty" }

        // Take the required number of distinct symbols
        val shuffledSymbols = availableSymbols.shuffled(random)
        val selectedSymbols = if (pairCount <= shuffledSymbols.size) {
            shuffledSymbols.take(pairCount)
        } else {
            // If pairCount exceeds available distinct symbols, cycle them
            (0 until pairCount).map { shuffledSymbols[it % shuffledSymbols.size] }
        }

        var attempts = 0
        var result: List<PairTile>

        do {
            val pairs = mutableListOf<Pair<Int, PairSymbol>>()
            for (pairIndex in 0 until pairCount) {
                val symbol = selectedSymbols[pairIndex]
                pairs.add(Pair(pairIndex, symbol))
                pairs.add(Pair(pairIndex, symbol))
            }

            val shuffledPairs = pairs.shuffled(random)
            result = shuffledPairs.mapIndexed { index, (pairId, symbol) ->
                PairTile(
                    id = index,
                    pairId = pairId,
                    symbol = symbol,
                    matched = false,
                    selected = false
                )
            }
            attempts++
        } while (attempts < 10 && previousTiles != null && isExactSameArrangement(result, previousTiles))

        return result
    }

    private fun isExactSameArrangement(current: List<PairTile>, previous: List<PairTile>): Boolean {
        if (current.size != previous.size) return false
        return current.indices.all { i ->
            current[i].symbol == previous[i].symbol && current[i].pairId == previous[i].pairId
        }
    }

    /**
     * Checks if two tiles are matching pairs.
     */
    fun isMatch(first: PairTile, second: PairTile): Boolean {
        return first.pairId == second.pairId
    }

    /**
     * Calculates base score for a match based on board progression multiplier.
     * multiplier = 1 + min(board - 1, 10) * 0.05
     * points = round(100 * multiplier)
     */
    fun calculateMatchScore(board: Int): Int {
        val multiplier = 1.0 + min(board - 1, 10) * 0.05
        return (100.0 * multiplier).roundToInt()
    }

    /**
     * Speed bonus based on remaining time.
     * speedBonus = remainingTimeSeconds * 5
     */
    fun calculateSpeedBonus(remainingTimeSeconds: Double): Int {
        return (remainingTimeSeconds * 5.0).toInt().coerceAtLeast(0)
    }

    /**
     * Streak bonus: every 5 consecutive correct pairs gives +100 bonus.
     */
    fun calculateStreakBonus(currentStreak: Int): Int {
        return if (currentStreak > 0 && currentStreak % 5 == 0) 100 else 0
    }

    /**
     * Get milestone title if current streak hit milestone.
     */
    fun getStreakMilestone(streak: Int): String? {
        return when (streak) {
            5 -> "GOOD!"
            10 -> "GREAT!"
            20 -> "ON FIRE!"
            30 -> "PAIR MASTER!"
            else -> null
        }
    }

    /**
     * Clamps remaining time between 0.0 and maxTime.
     */
    fun clampTime(time: Double, maxTime: Double): Double {
        return time.coerceIn(0.0, maxTime)
    }

    /**
     * Calculates accuracy percentage.
     */
    fun calculateAccuracy(correctMatches: Int, wrongAttempts: Int): Double {
        val total = correctMatches + wrongAttempts
        if (total <= 0) return 0.0
        return (correctMatches.toDouble() / total.toDouble()) * 100.0
    }

    /**
     * Determines pair count for continuous difficulty progression based on board number.
     * Board 1: startingPairs
     * Board 2: startingPairs
     * Board 3: min(startingPairs + 2, 12)
     * Board 4: min(startingPairs + 2, 12)
     * Board 5+: min(startingPairs + 4, 12) (max 12 pairs / 24 tiles)
     */
    fun getPairCountForBoard(startingPairs: Int, boardNumber: Int): Int {
        val additional = when {
            boardNumber <= 2 -> 0
            boardNumber <= 4 -> 2
            else -> 4
        }
        return min(startingPairs + additional, 12)
    }
}
