package com.babilonia.presentation.flow.main.search.common

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.model.enums.FilterType
import com.babilonia.presentation.flow.main.search.model.DisplaybleFilter
import com.babilonia.presentation.flow.main.search.model.PrefixedDisplayableFilter
import com.babilonia.presentation.flow.main.search.model.RangeFilter
import com.babilonia.presentation.flow.main.search.model.SingleFilter
import com.babilonia.presentation.utils.DisplayableFilterFormatter

// Created by Anton Yatsenko on 24.07.2019.
class FiltersDelegateImpl : FiltersDelegate {

    private var filters = mutableListOf<Filter>()
    private val filtersMap = mutableMapOf<String, Filter>()
    private val facilities = mutableListOf<Facility>()
    private var appliedFacilities = mutableListOf<Facility>()
    override val filtersLiveData = MutableLiveData<MutableList<DisplaybleFilter>>()

    override fun clearFilters() {
        filtersMap.clear()
        filters.clear()
        appliedFacilities.clear()
        facilities.clear()
        filtersLiveData.value = mutableListOf()
    }

    override fun addFilter(type: FilterType, value: String, displayValue: String) {
        filtersMap[type.type] = Filter(displayValue, type.type, value)
    }

    override fun addAndApplyFilter(filter: Filter) {
        filtersMap[filter.type] = filter
        filters = filtersMap.values.sortedByDescending { it.type == FilterType.LISTING.type }.toMutableList()
    }

    override fun getFilters(): List<Filter> {
        return filters
    }

    override fun getTempFilters(): List<Filter> {
        return filtersMap.values.sortedByDescending { it.type == FilterType.LISTING.type }.toMutableList()
    }

    override fun applyFilters(resources: Resources) {
        filters = filtersMap.values.sortedByDescending { it.type == FilterType.LISTING.type }.toMutableList()
        appliedFacilities.clear()
        appliedFacilities.addAll(facilities)
        filtersLiveData.value = getDisplayableFilters(resources)
    }

    override fun removeFilter(type: FilterType) {
        if (type == FilterType.FACILITY) {
            facilities.clear()
            appliedFacilities.clear()
        }
        filters.remove(filters.find { it.type == type.type })
        filtersMap.remove(type.type)
    }

    override fun removeFilter(type: String) {
        when (type) {
            FilterType.PRICE.type -> removePriceFilter()
            FilterType.FACILITY.type -> {
                appliedFacilities.clear()
                facilities.clear()
            }
            FilterType.YEAR_OF_CONSTRUCTION_START.type,
            FilterType.YEAR_OF_CONSTRUCTION_END.type -> removeYearOfConstructionFilter()
            FilterType.AREA_TOTAL_START.type,
            FilterType.AREA_TOTAL_END.type -> removeTotalAreaFilter()
            FilterType.AREA_BUILT_START.type,
            FilterType.AREA_BUILT_END.type -> removeBuiltAreaFilter()
            else -> {
                filters.remove(filters.find { it.type == type })
                filtersMap.remove(type)
            }
        }
    }

    override fun addFacility(facility: Facility) {
        facilities.add(facility)
    }

    override fun removeFacility(facility: Facility) {
        facilities.remove(facility)
    }

    override fun clearTempFacilities() {
        facilities.clear()
    }

    override fun getFacilities(): List<Facility> {
        return appliedFacilities
    }

    override fun getTempFacilities(): List<Facility> {
        return facilities
    }

    private fun removePriceFilter() {
        filters.remove(filters.find { it.type == FilterType.PRICE.type })
        filtersMap.remove(FilterType.PRICE.type)
        filters.remove(filters.find { it.type == FilterType.PRICE_START.type })
        filtersMap.remove(FilterType.PRICE_START.type)
        filters.remove(filters.find { it.type == FilterType.PRICE_END.type })
        filtersMap.remove(FilterType.PRICE_END.type)
    }

    private fun removeYearOfConstructionFilter() {
        filters.remove(filters.find { it.type == FilterType.YEAR_OF_CONSTRUCTION_START.type })
        filtersMap.remove(FilterType.YEAR_OF_CONSTRUCTION_START.type)
        filters.remove(filters.find { it.type == FilterType.YEAR_OF_CONSTRUCTION_END.type })
        filtersMap.remove(FilterType.YEAR_OF_CONSTRUCTION_END.type)
    }

    private fun removeTotalAreaFilter() {
        filters.remove(filters.find { it.type == FilterType.AREA_TOTAL_START.type })
        filtersMap.remove(FilterType.AREA_TOTAL_START.type)
        filters.remove(filters.find { it.type == FilterType.AREA_TOTAL_END.type })
        filtersMap.remove(FilterType.AREA_TOTAL_END.type)
    }

    private fun removeBuiltAreaFilter() {
        filters.remove(filters.find { it.type == FilterType.AREA_BUILT_START.type })
        filtersMap.remove(FilterType.AREA_BUILT_START.type)
        filters.remove(filters.find { it.type == FilterType.AREA_BUILT_END.type })
        filtersMap.remove(FilterType.AREA_BUILT_END.type)
    }

    private fun getDisplayableFilters(resources: Resources): MutableList<DisplaybleFilter> {
        val displayableFilters = mutableListOf<DisplaybleFilter>()
        filtersMap[FilterType.LISTING.type]?.run {
            val color = if (value == Constants.SALE) {
                R.color.sale_color
            } else {
                R.color.rent_color
            }
            displayableFilters.add(
                SingleFilter(
                    displayedName,
                    type,
                    color,
                    false,
                    android.R.color.white,
                    R.style.ChipTextAppearance
                )
            )
        }

        createDisplayableFilter(resources, FilterType.PROPERTY)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.PRICE)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.AREA_TOTAL_START)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.AREA_BUILT_START)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.YEAR_OF_CONSTRUCTION_START)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.BATHROOMS)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.BEDROOMS)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.TOTAL_FLOORS)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.FLOOR_NUMBER)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.PARKING)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.PARKING_FOR_VISITORS)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.WAREHOUSE)?.let { displayableFilters.add(it) }
        createDisplayableFilter(resources, FilterType.FACILITY)?.let { displayableFilters.add(it) }

        return displayableFilters
    }

    override fun hasFilters() = filters.isNotEmpty()

    override fun hasTempFilters() = filtersMap.isNotEmpty()

    override fun getFilter(type: FilterType): Filter? {
        return filters.firstOrNull { it.type == type.type }
    }

    override fun getTempFilter(filterType: FilterType): Filter? {
        return filtersMap[filterType.type]
    }

    private fun createSingleFilter(filterType: String, filterText: String): SingleFilter {
        return SingleFilter(
            filterText,
            filterType,
            R.color.pale_grey,
            true,
            R.color.gunmetal,
            R.style.ChipTextAppearanceNormal
        )
    }

    private fun createPrefixedFilter(
        filterType: String, filterText: String, prefixText: String
    ): PrefixedDisplayableFilter {
        return PrefixedDisplayableFilter(
            filterText,
            filterType,
            R.color.pale_grey,
            true,
            R.color.gunmetal,
            R.style.ChipTextAppearanceNormal,
            prefixText,
            R.color.steel
        )
    }

    private fun createDisplayableFilter(resources: Resources, filterType: FilterType): DisplaybleFilter? {
        return when (filterType) {
            FilterType.PROPERTY,
            FilterType.PARKING_FOR_VISITORS,
            FilterType.WAREHOUSE -> filtersMap[filterType.type]?.let {
                createSingleFilter(it.type, it.displayedName)
            }
            FilterType.PRICE,
            FilterType.PRICE_START,
            FilterType.PRICE_END -> {
                val priceStart = filtersMap[FilterType.PRICE_START.type]?.value ?: EmptyConstants.EMPTY_STRING
                val priceEnd = filtersMap[FilterType.PRICE_END.type]?.value ?: EmptyConstants.EMPTY_STRING
                RangeFilter(priceStart, priceEnd)
            }
            FilterType.BEDROOMS,
            FilterType.BATHROOMS,
            FilterType.PARKING,
            FilterType.FLOOR_NUMBER,
            FilterType.TOTAL_FLOORS -> filtersMap[filterType.type]?.let {
                createSingleFilter(it.type, DisplayableFilterFormatter.format(resources, it.type, it.value))
            }
            FilterType.FACILITY -> {
                if (appliedFacilities.size > 0) {
                    createSingleFilter(
                        filterType.type,
                        DisplayableFilterFormatter.format(
                            resources,
                            filterType.type,
                            appliedFacilities.size
                        )
                    )
                } else {
                    null
                }
            }
            FilterType.AREA_TOTAL_START,
            FilterType.AREA_TOTAL_END -> {
                val start = filtersMap[FilterType.AREA_TOTAL_START.type]
                val end = filtersMap[FilterType.AREA_TOTAL_END.type]

                if (start == null || end == null) {
                    null
                } else {
                    createPrefixedFilter(
                        filterType.type,
                        DisplayableFilterFormatter.format(
                            resources,
                            filterType.type,
                            start.value,
                            end.value
                        ),
                        resources.getString(R.string.chip_prefix_area_total)
                    )
                }
            }
            FilterType.AREA_BUILT_START,
            FilterType.AREA_BUILT_END -> {
                val start = filtersMap[FilterType.AREA_BUILT_START.type]
                val end = filtersMap[FilterType.AREA_BUILT_END.type]

                if (start == null || end == null) {
                    null
                } else {
                    createPrefixedFilter(
                        filterType.type,
                        DisplayableFilterFormatter.format(
                            resources,
                            filterType.type,
                            start.value,
                            end.value
                        ),
                        resources.getString(R.string.chip_prefix_area_built)
                    )
                }
            }
            FilterType.YEAR_OF_CONSTRUCTION_START,
            FilterType.YEAR_OF_CONSTRUCTION_END -> {
                val start = filtersMap[FilterType.YEAR_OF_CONSTRUCTION_START.type]
                val end = filtersMap[FilterType.YEAR_OF_CONSTRUCTION_END.type]

                if (start == null || end == null) {
                    null
                } else {
                    createSingleFilter(
                        filterType.type,
                        DisplayableFilterFormatter.format(
                            resources,
                            filterType.type,
                            start.value,
                            end.value
                        )
                    )
                }
            }
            else -> null
        }
    }
}