package com.example.quickpair

import com.example.quickpair.model.PairSymbol
import com.example.quickpair.model.PairTile
import com.example.quickpair.model.QuickPairEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuickPairEngineTest {

    @Test
    fun boardTest_pairCount6_totalTiles12_andEveryPairTwice() {
        val board = QuickPairEngine.generateBoard(pairCount = 6)
        assertEquals("Total tiles must be 12 for 6 pairs", 12, board.size)

        val pairCounts = board.groupingBy { it.pairId }.eachCount()
        assertEquals("There must be exactly 6 distinct pairs", 6, pairCounts.size)
        pairCounts.forEach { (pairId, count) ->
            assertEquals("Pair $pairId must appear exactly twice", 2, count)
        }
    }

    @Test
    fun pairTest_generate100Boards_verifyInvariants() {
        for (i in 1..100) {
            val random = Random(i * 31L)
            val pairCount = listOf(6, 8, 10, 12).random(random)
            val board = QuickPairEngine.generateBoard(pairCount = pairCount, random = random)

            // 1. Total tiles = pairCount * 2
            assertEquals(pairCount * 2, board.size)

            // 2. All tile IDs are unique
            val uniqueIds = board.map { it.id }.toSet()
            assertEquals(board.size, uniqueIds.size)

            // 3. Every pair has exactly two tiles, no triple, no unmatched
            val pairGrouping = board.groupBy { it.pairId }
            assertEquals(pairCount, pairGrouping.size)

            pairGrouping.forEach { (pairId, tiles) ->
                assertEquals("Pair $pairId must have exactly 2 tiles", 2, tiles.size)
                // Both tiles must have the same symbol
                assertEquals(tiles[0].symbol, tiles[1].symbol)
            }
        }
    }

    @Test
    fun matchTest_samePairId_returnsTrue() {
        val first = PairTile(id = 1, pairId = 4, symbol = PairSymbol.STAR)
        val second = PairTile(id = 8, pairId = 4, symbol = PairSymbol.STAR)

        val result = QuickPairEngine.isMatch(first, second)
        assertTrue("Matching pairIds must return true", result)
    }

    @Test
    fun wrongMatchTest_differentPairId_returnsFalse() {
        val first = PairTile(id = 1, pairId = 4, symbol = PairSymbol.STAR)
        val second = PairTile(id = 5, pairId = 7, symbol = PairSymbol.DIAMOND)

        val result = QuickPairEngine.isMatch(first, second)
        assertFalse("Different pairIds must return false", result)
    }

    @Test
    fun timeValidation_clampingLimits() {
        // Clamping above max
        val clampedHigh = QuickPairEngine.clampTime(28.0, 25.0)
        assertEquals(25.0, clampedHigh, 0.001)

        // Clamping below zero
        val clampedLow = QuickPairEngine.clampTime(-2.5, 20.0)
        assertEquals(0.0, clampedLow, 0.001)

        // Within range
        val clampedNormal = QuickPairEngine.clampTime(14.2, 20.0)
        assertEquals(14.2, clampedNormal, 0.001)
    }

    @Test
    fun accuracyCalculation() {
        // 10 correct, 0 wrong -> 100%
        val perfect = QuickPairEngine.calculateAccuracy(10, 0)
        assertEquals(100.0, perfect, 0.01)

        // 91 correct, 9 wrong -> 91.0%
        val ninetyOne = QuickPairEngine.calculateAccuracy(91, 9)
        assertEquals(91.0, ninetyOne, 0.01)

        // 0 correct, 0 wrong -> 0.0%
        val zero = QuickPairEngine.calculateAccuracy(0, 0)
        assertEquals(0.0, zero, 0.01)
    }

    @Test
    fun streakBonusAndMilestones() {
        // Every 5 consecutive correct pairs gets +100 bonus
        assertEquals(0, QuickPairEngine.calculateStreakBonus(1))
        assertEquals(0, QuickPairEngine.calculateStreakBonus(4))
        assertEquals(100, QuickPairEngine.calculateStreakBonus(5))
        assertEquals(0, QuickPairEngine.calculateStreakBonus(6))
        assertEquals(100, QuickPairEngine.calculateStreakBonus(10))

        // Milestones
        assertEquals("GOOD!", QuickPairEngine.getStreakMilestone(5))
        assertEquals("GREAT!", QuickPairEngine.getStreakMilestone(10))
        assertEquals("ON FIRE!", QuickPairEngine.getStreakMilestone(20))
        assertEquals("PAIR MASTER!", QuickPairEngine.getStreakMilestone(30))
        assertNull(QuickPairEngine.getStreakMilestone(7))
    }

    @Test
    fun scoreMultiplier() {
        // Board 1: multiplier = 1 + 0 * 0.05 = 1.0 -> 100
        assertEquals(100, QuickPairEngine.calculateMatchScore(1))

        // Board 3: multiplier = 1 + 2 * 0.05 = 1.10 -> 110
        assertEquals(110, QuickPairEngine.calculateMatchScore(3))

        // Board 11+: capped at min(board - 1, 10) = 10 -> multiplier = 1.50 -> 150
        assertEquals(150, QuickPairEngine.calculateMatchScore(11))
        assertEquals(150, QuickPairEngine.calculateMatchScore(20))
    }

    @Test
    fun continuousProgression_pairCount() {
        val startPairs = 6
        assertEquals(6, QuickPairEngine.getPairCountForBoard(startPairs, 1))
        assertEquals(6, QuickPairEngine.getPairCountForBoard(startPairs, 2))
        assertEquals(8, QuickPairEngine.getPairCountForBoard(startPairs, 3))
        assertEquals(8, QuickPairEngine.getPairCountForBoard(startPairs, 4))
        assertEquals(10, QuickPairEngine.getPairCountForBoard(startPairs, 5))
        assertEquals(10, QuickPairEngine.getPairCountForBoard(startPairs, 6))

        // Never exceeds maximum of 12 pairs (24 tiles)
        val hardStart = 12
        assertEquals(12, QuickPairEngine.getPairCountForBoard(hardStart, 5))
    }
}
