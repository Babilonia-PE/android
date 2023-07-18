package com.babilonia.presentation.utils

import android.content.res.Resources
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.enums.FilterType
import java.util.*

object DisplayableFilterFormatter {

    fun format(res: Resources, filterType: String, value: String): String {
        return format(res, filterType, value.toInt())
    }

    fun format(res: Resources, filterType: String, value: Int): String {
        return when (filterType) {
            FilterType.BEDROOMS.type -> {
                res.getQuantityString(R.plurals.bedrooms_plural, value, value)
            }
            FilterType.BATHROOMS.type -> {
                res.getQuantityString(R.plurals.bathrooms_plural, value, value)
            }
            FilterType.TOTAL_FLOORS.type -> {
                res.getQuantityString(R.plurals.floors_plural, value, value)
            }
            FilterType.FLOOR_NUMBER.type -> {
                res.getString(R.string.floor_number_numb, value, value)
            }
            FilterType.PARKING.type -> {
                res.getQuantityString(R.plurals.parkings_short_plural, value, value)
            }
            FilterType.FACILITY.type -> {
                res.getQuantityString(R.plurals.facilities_plural, value, value)
            }
            else -> EmptyConstants.EMPTY_STRING
        }
    }

    fun format(res: Resources, filterType: String, startValue: String, endValue: String): String {
        return when (filterType) {
            FilterType.YEAR_OF_CONSTRUCTION_START.type,
            FilterType.YEAR_OF_CONSTRUCTION_END.type -> {
                res.getString(R.string.chip_template_year_of_construction, startValue, endValue)
            }
            FilterType.AREA_TOTAL_START.type,
            FilterType.AREA_TOTAL_END.type,
            FilterType.AREA_BUILT_START.type,
            FilterType.AREA_BUILT_END.type -> {
                res.getString(R.string.chip_template_area_filter, addSeparators(startValue), addSeparators(endValue))
            }
            else -> EmptyConstants.EMPTY_STRING
        }
    }

    private fun addSeparators(value: String): String {
        return String.format(Locale.US, "%,d", value.toInt())
    }
}