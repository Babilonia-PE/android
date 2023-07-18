package com.babilonia.presentation.flow.main.search.model

import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.presentation.utils.PriceFormatter

// Created by Anton Yatsenko on 24.07.2019.
class RangeFilter(val start: String, val end: String) : DisplaybleFilter {
    override val backgroundColor: Int = R.color.pale_grey
    override val clickable: Boolean = true
    override val textColor: Int = R.color.gunmetal
    override val textStyle: Int = R.style.ChipTextAppearance
    override val type: String = EmptyConstants.EMPTY_STRING
    override val value: String
        get() {
            return when {
                start.isEmpty() && end.isEmpty() -> EmptyConstants.EMPTY_STRING
                start.isEmpty() -> "$${PriceFormatter.format(end.toLong())}"
                end.isEmpty() -> "$${PriceFormatter.format(start.toLong())}"
                else -> "$${PriceFormatter.format(start.toLong())} - $${PriceFormatter.format(end.toLong())}"
            }

        }
}