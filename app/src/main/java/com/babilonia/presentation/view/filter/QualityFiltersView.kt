package com.babilonia.presentation.view.filter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.domain.model.enums.FilterType
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel
import com.babilonia.presentation.flow.main.search.model.FiltersVisibility
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import com.babilonia.presentation.view.numberpicker.ClickNumberPickerListener
import com.babilonia.presentation.view.numberpicker.PickerClickType
import kotlinx.android.synthetic.main.layout_filter_two_side.view.*
import kotlinx.android.synthetic.main.layout_filters_quality.view.*
import java.util.*

class QualityFiltersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var viewModel: ListingSearchViewModel

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filters_quality, this, true)
    }

    fun initQualityFilters(viewModel: ListingSearchViewModel) {
        this.viewModel = viewModel
        initPickers()
        initRangeBarFilters()
        initCheckFilters()
        setPredefinedFilters()
    }

    fun setMetadata(metadata: ListingsMetadata) {
        filterTotalArea.setRange(minValue = 0, maxValue = metadata.maxTotalArea)
        filterBuiltArea.setRange(minValue = 0, maxValue = metadata.maxBuiltArea)
        setPredefinedAreaFilters()
    }

    fun setVisibility(visibility: FiltersVisibility) {
        filterTotalArea.visibleOrGone(visibility.isTotalAreaVisible)
        filterBuiltArea.visibleOrGone(visibility.isBuiltAreaVisible)
        filterYearOfConstruction.visibleOrGone(visibility.isYearOfConstructionVisible)
        pickerBathrooms.visibleOrGone(visibility.isBathroomsVisible)
        pickerBedrooms.visibleOrGone(visibility.isBedroomsVisible)
        pickerFloors.visibleOrGone(visibility.isTotalFloorsVisible)
        pickerFloorNumber.visibleOrGone(visibility.isFloorNumberVisible)
        pickerParkingSlots.visibleOrGone(visibility.isParkingVisible)
        filterParkingForVisitors.visibleOrGone(visibility.isParkingForVisitorsVisible)
        filterWarehouse.visibleOrGone(visibility.isWarehouseVisible)
    }

    fun resetFilters() {
        filterTotalArea.resetToInitialPositions()
        filterBuiltArea.resetToInitialPositions()
        filterYearOfConstruction.resetToInitialPositions()
        pickerBathrooms.setPickerValue(0)
        pickerBedrooms.setPickerValue(0)
        pickerFloors.setPickerValue(0)
        pickerFloorNumber.setPickerValue(0)
        pickerParkingSlots.setPickerValue(0)
        filterParkingForVisitors.setChecked(false)
        filterWarehouse.setChecked(false)
    }

    private fun setPredefinedAreaFilters() {
        viewModel.getFilter(FilterType.AREA_TOTAL_START)?.let {
            filterTotalArea.setCurrentStartValue(it.value)
        }
        viewModel.getFilter(FilterType.AREA_TOTAL_END)?.let {
            filterTotalArea.setCurrentEndValue(it.value)
        }

        viewModel.getFilter(FilterType.AREA_BUILT_START)?.let {
            filterBuiltArea.setCurrentStartValue(it.value)
        }
        viewModel.getFilter(FilterType.AREA_BUILT_END)?.let {
            filterBuiltArea.setCurrentEndValue(it.value)
        }
    }

    private fun setPredefinedFilters() {
        setPredefinedAreaFilters()

        viewModel.getFilter(FilterType.YEAR_OF_CONSTRUCTION_START)?.let {
            filterYearOfConstruction.setCurrentStartValue(it.value)
        }
        viewModel.getFilter(FilterType.YEAR_OF_CONSTRUCTION_END)?.let {
            filterYearOfConstruction.setCurrentEndValue(it.value)
        }

        viewModel.getFilter(FilterType.BEDROOMS)?.let {
            pickerBedrooms.setPickerValue(Integer.parseInt(it.value))
        }
        viewModel.getFilter(FilterType.BATHROOMS)?.let {
            pickerBathrooms.setPickerValue(Integer.parseInt(it.value))
        }
        viewModel.getFilter(FilterType.TOTAL_FLOORS)?.let {
            pickerFloors.setPickerValue(Integer.parseInt(it.value))
        }
        viewModel.getFilter(FilterType.FLOOR_NUMBER)?.let {
            pickerFloorNumber.setPickerValue(Integer.parseInt(it.value))
        }
        viewModel.getFilter(FilterType.PARKING)?.let {
            pickerParkingSlots.setPickerValue(Integer.parseInt(it.value))
        }
        // for CheckFilters we only keep filter if it is check. So we don't need to parse filter's
        // value. If it exists in ViewModel then it is checked.
        viewModel.getFilter(FilterType.PARKING_FOR_VISITORS)?.let {
            filterParkingForVisitors.setChecked(true)
        }
        viewModel.getFilter(FilterType.WAREHOUSE)?.let {
            filterWarehouse.setChecked(true)
        }
    }

    private fun initPickers() {
        pickerBathrooms.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(
                previousValue: Int,
                currentValue: Int,
                pickerClickType: PickerClickType
            ) {
                changeFilterValue(FilterType.BATHROOMS, currentValue.toString(), currentValue == 0)
            }
        })
        pickerBedrooms.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(
                previousValue: Int,
                currentValue: Int,
                pickerClickType: PickerClickType
            ) {
                changeFilterValue(FilterType.BEDROOMS, currentValue.toString(), currentValue == 0)
            }
        })
        pickerFloors.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(
                previousValue: Int,
                currentValue: Int,
                pickerClickType: PickerClickType
            ) {
                changeFilterValue(FilterType.TOTAL_FLOORS, currentValue.toString(), currentValue == 0)
            }
        })
        pickerFloorNumber.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(
                previousValue: Int,
                currentValue: Int,
                pickerClickType: PickerClickType
            ) {
                changeFilterValue(FilterType.FLOOR_NUMBER, currentValue.toString(), currentValue == 0)
            }
        })
        pickerParkingSlots.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(
                previousValue: Int,
                currentValue: Int,
                pickerClickType: PickerClickType
            ) {
                changeFilterValue(FilterType.PARKING, currentValue.toString(), currentValue == 0)
            }
        })
    }

    private fun initRangeBarFilters() {
        filterTotalArea.apply {
            setTitleText(R.string.total_area_square_meters)
            initListeners(
                onLeftPinChanged = { newValue, isInInitialPosition ->
                    changeRangeBarValue(FilterType.AREA_TOTAL_START, FilterType.AREA_TOTAL_END, this)
                },
                onRightPinChanged = { newValue, isInInitialPosition ->
                    changeRangeBarValue(FilterType.AREA_TOTAL_START, FilterType.AREA_TOTAL_END, this)
                }
            )
        }
        filterBuiltArea.apply {
            setTitleText(R.string.built_area_square_meters)
            initListeners(
                onLeftPinChanged = { newValue, isInInitialPosition ->
                    changeRangeBarValue(FilterType.AREA_BUILT_START, FilterType.AREA_BUILT_END, this)
                },
                onRightPinChanged = { newValue, isInInitialPosition ->
                    changeRangeBarValue(FilterType.AREA_BUILT_START, FilterType.AREA_BUILT_END, this)
                }
            )
        }
        filterYearOfConstruction.apply {
            rangeBar.visibleOrGone(false)
            setFormatWithComas(false)
            setRange(Constants.MIN_YEAR, Calendar.getInstance().get(Calendar.YEAR))
            setTitleText(R.string.year_of_construction)
            setInputHints(R.string.from, R.string.to)
            initListeners(
                onLeftPinChanged = { newValue, isInInitialPosition ->
                    changeRangeBarValue(FilterType.YEAR_OF_CONSTRUCTION_START, FilterType.YEAR_OF_CONSTRUCTION_END, this)
                },
                onRightPinChanged = { newValue, isInInitialPosition ->
                    changeRangeBarValue(FilterType.YEAR_OF_CONSTRUCTION_START, FilterType.YEAR_OF_CONSTRUCTION_END, this)
                }
            )
        }
    }

    private fun initCheckFilters() {
        filterParkingForVisitors.setOnCheckChangedCallback {  isChecked ->
            when (isChecked) {
                true -> viewModel.addFilter(FilterType.PARKING_FOR_VISITORS,
                    Constants.CHECKED_FILTER_VALUE, context.getString(R.string.parking_for_visitors_short))
                false -> viewModel.removeFilter(FilterType.PARKING_FOR_VISITORS)
            }
            viewModel.metadataUpdateSubject.onNext(true)
        }
        filterWarehouse.setOnCheckChangedCallback {  isChecked ->
            when (isChecked) {
                true -> viewModel.addFilter(FilterType.WAREHOUSE,
                    Constants.CHECKED_FILTER_VALUE, context.getString(R.string.warehouse))
                false -> viewModel.removeFilter(FilterType.WAREHOUSE)
            }
            viewModel.metadataUpdateSubject.onNext(true)
        }
    }

    private fun changeFilterValue(filterType: FilterType, newValue: String, isInInitialPosition: Boolean) {
        when (isInInitialPosition) {
            true -> viewModel.removeFilter(filterType)
            false -> viewModel.addFilter(filterType, newValue, newValue)
        }
        viewModel.metadataUpdateSubject.onNext(true)
    }

    private fun changeRangeBarValue(
        filterTypeStart: FilterType,
        filterTypeEnd: FilterType,
        rangeBar: TwoSideFilter
    ) {
        with (rangeBar) {
            when (isLeftPinInInitialPosition() && isRightPinInInitialPosition()) {
                true -> {
                    viewModel.removeFilter(filterTypeStart)
                    viewModel.removeFilter(filterTypeEnd)
                }
                false -> {
                    viewModel.addFilter(filterTypeStart, getLeftValueText(), getLeftValueText())
                    viewModel.addFilter(filterTypeEnd, getRightValueText(), getRightValueText())
                }
            }
        }
        viewModel.metadataUpdateSubject.onNext(true)
    }
}