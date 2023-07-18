package com.babilonia.presentation.utils

import java.util.*
import kotlin.math.round

// Created by Anton Yatsenko on 25.07.2019.
object PriceFormatter {

    const val PRICE_TWO_DIGITS_AFTER_POINT = "%.02f"

    private val suffixes: NavigableMap<Long, String> = TreeMap()

    init {
        suffixes[1_000L] = "K"
        suffixes[1_000_000L] = "M"
        suffixes[1_000_000_000L] = "G"
        suffixes[1_000_000_000_000L] = "T"
        suffixes[1_000_000_000_000_000L] = "P"
        suffixes[1_000_000_000_000_000_000L] = "E"
    }


    fun format(value: Long): String {
        if (value == java.lang.Long.MIN_VALUE) return format(java.lang.Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return java.lang.Long.toString(value) //deal with easy case

        val e = suffixes.floorEntry(value)
        val divideBy = e.key
        val suffix = e.value

        val truncated = round(value / (divideBy!! / 10.0)).toLong() //rounded number part of the output times 10
        val hasDecimal = truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) "${truncated / 10.0}$suffix" else "${truncated / 10}$suffix"
    }
}
