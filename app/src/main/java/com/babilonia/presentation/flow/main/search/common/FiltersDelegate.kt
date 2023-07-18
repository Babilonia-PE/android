package com.babilonia.presentation.flow.main.search.common

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.model.enums.FilterType
import com.babilonia.presentation.flow.main.search.model.DisplaybleFilter

// Created by Anton Yatsenko on 24.07.2019.
interface FiltersDelegate {
    val filtersLiveData: MutableLiveData<MutableList<DisplaybleFilter>>
    fun addFilter(type: FilterType, value: String, displayValue: String)
    fun applyFilters(resources: Resources)
    fun clearFilters()
    fun removeFilter(type: FilterType)
    fun removeFilter(type: String)
    fun addFacility(facility: Facility)
    fun removeFacility(facility: Facility)
    fun clearTempFacilities()
    fun getFilters(): List<Filter>
    fun getTempFilters(): List<Filter>
    fun getFilter(type: FilterType): Filter?
    fun getTempFilter(filterType: FilterType): Filter?
    fun getTempFacilities(): List<Facility>
    fun getFacilities(): List<Facility>
    fun hasFilters(): Boolean
    fun hasTempFilters(): Boolean
}