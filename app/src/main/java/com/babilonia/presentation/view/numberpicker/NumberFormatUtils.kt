package com.babilonia.presentation.view.numberpicker

// Created by Anton Yatsenko on 04.06.2019.
object NumberFormatUtils {

    fun provideFloatFormater(decimalLength: Int): String {
        val builder = StringBuilder()
        builder.append("%.")
        builder.append(decimalLength.toString())
        builder.append("f")
        return builder.toString()
    }

}