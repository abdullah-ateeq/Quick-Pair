package com.example.quickpair.model

data class PairTile(
    val id: Int,
    val pairId: Int,
    val symbol: PairSymbol,
    val matched: Boolean = false,
    val selected: Boolean = false
)
