package com.babilonia.data.mapper

import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.model.enums.FilterType
import javax.inject.Inject

// Created by Anton Yatsenko on 23.07.2019.
class FiltersMapper @Inject constructor() {
    fun mapToQuery(filters: List<Filter>): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()
        filters.forEach {
            queryMap[it.type] = it.value
        }
        return queryMap
    }

    fun mapToHistogramQuery(filters: List<Filter>): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()
        filters.forEach {
            if (it.type != FilterType.PRICE_END.type &&
                it.type != FilterType.PRICE_START.type
            ) {
                queryMap[it.type] = it.value
            }
        }
        return queryMap
    }

    fun mapFacilitiesToQuery(facilities: List<Facility>): List<Int> {
        return facilities.map { it.id }
    }
}