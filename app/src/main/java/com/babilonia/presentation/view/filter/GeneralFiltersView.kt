package com.babilonia.presentation.view.filter

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.enums.FilterType
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel
import com.babilonia.presentation.utils.PriceFormatter
import com.babilonia.presentation.view.dialog.SingleChoiceAlertDialog
import com.babilonia.presentation.view.priceview.BarEntry
import com.babilonia.presentation.view.priceview.RANGE_BAR_END_VALUE
import com.babilonia.presentation.view.priceview.RANGE_BAR_START_VALUE
import kotlinx.android.synthetic.main.item_range_bar.view.*
import kotlinx.android.synthetic.main.layout_filters_general.view.*

class GeneralFiltersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var viewModel: ListingSearchViewModel

    private var currentRangeBarLeftIndex: Int = RANGE_BAR_START_VALUE
    private var currentRangeBarRightIndex: Int = RANGE_BAR_END_VALUE
    private var currentRangeBarLeftValue: Long = EmptyConstants.EMPTY_LONG
    private var currentRangeBarRightValue: Long = EmptyConstants.EMPTY_LONG
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filters_general, this, true)
    }

    fun initGeneralFilters(viewModel: ListingSearchViewModel) {
        this.viewModel = viewModel
        initDefaultFilters()
        initToggleButtons()
        initPropertyTypes()
    }

    fun initDefaultFilters() {
        if (viewModel.hasFilters().not()) {
            viewModel.addFilter(FilterType.LISTING, Constants.SALE, context.getString(R.string.sale))
            setSaleChecked()
            etPropertyType.setText(resources.getStringArray(R.array.property_types_for_sorting).first())
        } else {
            setPredefinedFilters()
        }
    }

    private fun addPropertyToFilters(title: String, selectedIndex: Int) {
        etPropertyType.setText(title)
        if (selectedIndex == 0) {
            viewModel.removeFilter(FilterType.PROPERTY)
            viewModel.getFacilities(Constants.ALL_FACILITIES)
            viewModel.onPropertyTypeChanged(Constants.ALL_FACILITIES)
        } else {
            val localizedProperties = resources.getStringArray(R.array.property_types)
            val selectedPropertyIndex = localizedProperties.indexOf(title)
            val requestPropertyName = if (selectedPropertyIndex == -1) {
                Constants.ALL_FACILITIES
            } else {
                PropertyType.getPropertyName(selectedPropertyIndex)
            }
            viewModel.addFilter(FilterType.PROPERTY, requestPropertyName, title)
            viewModel.getFacilities(requestPropertyName)
            viewModel.onPropertyTypeChanged(requestPropertyName)
        }
        viewModel.metadataUpdateSubject.onNext(true)
    }

    private fun setPredefinedFilters() {
        if (viewModel.getFilter(FilterType.LISTING)?.value == Constants.SALE) {
            setSaleChecked()
        } else {
            setRentChecked()
        }
        if (viewModel.getFilter(FilterType.PROPERTY) == null) {
            etPropertyType.setText(resources.getStringArray(R.array.property_types_for_sorting).first())
        } else {
            etPropertyType.setText(viewModel.getFilter(FilterType.PROPERTY)?.displayedName)
        }
    }

    fun initRangeBar(rangeBarEntries: List<BarEntry>) {
        val firstEntry = rangeBarEntries.first()
        val lastEntry = rangeBarEntries.last()

        if (currentRangeBarLeftValue == EmptyConstants.EMPTY_LONG && currentRangeBarRightValue == EmptyConstants.EMPTY_LONG) {
            currentRangeBarLeftValue = firstEntry.priceStart.toLong()
            currentRangeBarRightValue = lastEntry.priceStart.toLong()
        }

        tvPriceFrom.text = "$${PriceFormatter.format(firstEntry.priceStart.toLong())}"
        tvPriceTo.text = "$${PriceFormatter.format(lastEntry.priceStart.toLong())}+"

        rangeBar.onLeftPinChanged = null
        rangeBar.onRightPinChanged = null

        rangeBar.setEntries(rangeBarEntries.toMutableList())
        if (viewModel.hasFilters() || viewModel.hasTempFilters()) {
            setPredefinedRange(rangeBarEntries)
        }
        rangeBar.onLeftPinChanged = { index, leftPinValue ->
            onLeftPinChanged(index, leftPinValue ?: EmptyConstants.EMPTY_LONG)
            tvPriceFrom.text = leftPinValue?.let { "$${PriceFormatter.format(it)}" }
            viewModel.metadataUpdateSubject.onNext(true)
        }

        rangeBar.onRightPinChanged = { index, rightPinValue ->
            onRightPinChanged(index, rightPinValue ?: EmptyConstants.EMPTY_LONG)
            tvPriceTo.text =
                if (index == RANGE_BAR_END_VALUE) "$${PriceFormatter.format(rangeBarEntries.last().priceStart.toLong())}+" else rightPinValue?.let {
                    "$${PriceFormatter.format(it)}"
                }
            viewModel.metadataUpdateSubject.onNext(true)
        }

        rangeBar.onSelectedEntriesSizeChanged = { selectedEntriesSize ->
            println("$selectedEntriesSize")
        }
        var totalSelectedSize = 0
        rangeBarEntries.forEach { entry ->
            totalSelectedSize += entry.listingsFound
        }
    }

    private fun onRightPinChanged(newIndex: Int, newValue: Long) {
        currentRangeBarRightIndex = newIndex
        currentRangeBarRightValue = newValue

        if (newIndex == RANGE_BAR_END_VALUE && currentRangeBarLeftIndex == RANGE_BAR_START_VALUE) {
            resetPriceFilters()
        } else {
            viewModel.addFilter(FilterType.PRICE_START, currentRangeBarLeftValue.toString(), currentRangeBarLeftValue.toString())
            viewModel.addFilter(FilterType.PRICE_END, newValue.toString(), newValue.toString())
        }
    }

    private fun onLeftPinChanged(newIndex: Int, newValue: Long) {
        currentRangeBarLeftIndex = newIndex
        currentRangeBarLeftValue = newValue

        if (newIndex == RANGE_BAR_START_VALUE && currentRangeBarRightIndex == RANGE_BAR_END_VALUE) {
            resetPriceFilters()
        } else {
            viewModel.addFilter(FilterType.PRICE_START, newValue.toString(), newValue.toString())
            viewModel.addFilter(FilterType.PRICE_END, currentRangeBarRightValue.toString(), currentRangeBarRightValue.toString())
        }
    }

    fun resetRangeBar() {
        rangeBar.apply {
            elementRangeBar.start = 0
            elementRangeBar.end = (viewModel.priceRangeLiveData.value?.size?.plus(1)) ?: EmptyConstants.EMPTY_INT
            onRangeChange(elementRangeBar.start, elementRangeBar.end)
        }
    }

    private fun setPredefinedRange(rangeBarEntries: List<BarEntry>) {
        var priceStartFilter = viewModel.getTempFilter(FilterType.PRICE_START)
        var priceEndFilter = viewModel.getTempFilter(FilterType.PRICE_END)

        if (priceStartFilter == null && priceEndFilter == null) {
            priceStartFilter = viewModel.getFilter(FilterType.PRICE_START)
            priceEndFilter = viewModel.getFilter(FilterType.PRICE_END)
        }

        if (priceStartFilter == null && priceEndFilter == null) return

        rangeBar.apply {
            val startIndex = rangeBarEntries.indexOf(rangeBarEntries.firstOrNull {
                it.priceStart == priceStartFilter?.value?.toInt()
            })
            elementRangeBar.start = if (startIndex == EmptyConstants.EMPTY_INT) 0 else startIndex
            val endIndex = rangeBarEntries.indexOf(rangeBarEntries.firstOrNull {
                it.priceStart == priceEndFilter?.value?.toInt()
            })
            elementRangeBar.end = if (endIndex == EmptyConstants.EMPTY_INT) rangeBarEntries.size + 1 else endIndex

            currentRangeBarLeftIndex = if (startIndex == EmptyConstants.EMPTY_INT) 0 else startIndex
            currentRangeBarRightIndex = if (endIndex == EmptyConstants.EMPTY_INT) rangeBarEntries.size - 1 else endIndex
            currentRangeBarLeftValue = rangeBarEntries[currentRangeBarLeftIndex].priceStart.toLong()
            currentRangeBarRightValue = rangeBarEntries[currentRangeBarRightIndex].priceStart.toLong()

            onRangeChange(elementRangeBar.start, elementRangeBar.end) // TODO check if this is not broken
        }

        priceStartFilter?.let {
            tvPriceFrom.text = "$${PriceFormatter.format(it.value.toLong())}"
        }
        priceEndFilter?.let {
            tvPriceTo.text = "$${PriceFormatter.format(it.value.toLong())}"
        }
    }

    private fun initToggleButtons() {
        btRent.setOnClickListener {
            setRentChecked()
            resetPriceFilters()
            viewModel.metadataUpdateSubject.onNext(true)
        }
        btSale.setOnClickListener {
            setSaleChecked()
            resetPriceFilters()
            viewModel.metadataUpdateSubject.onNext(true)
        }
    }

    private fun resetPriceFilters() {
        viewModel.removeFilter(FilterType.PRICE_START)
        viewModel.removeFilter(FilterType.PRICE_END)
    }

    private fun setRentChecked() {
        btSale.isEnabled = true
        btRent.isChecked = true
        btSale.isChecked = false
        btRent.isEnabled = false
        viewModel.addFilter(FilterType.LISTING, Constants.RENT, context.getString(R.string.rent))
    }

    private fun setSaleChecked() {
        btRent.isEnabled = true
        btSale.isChecked = true
        btSale.isEnabled = false
        btRent.isChecked = false
        viewModel.addFilter(FilterType.LISTING, Constants.SALE, context.getString(R.string.sale))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPropertyTypes() {
        etPropertyType.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showPropertyDialog()
            }
            false
        }
    }

    private fun showPropertyDialog() {
        val array = resources.getStringArray(R.array.property_types_for_sorting)
        var selectedIndex = array.indexOf(etPropertyType.text.toString())

        context?.let {
            SingleChoiceAlertDialog.Builder(it)
                .setTitleText(it.getString(R.string.property_type))
                .setSingleChoiceItems(array, selectedIndex) { which ->
                    selectedIndex = which
                }
                .setRightButton(it.getString(R.string.ok)) {
                    val title = array[selectedIndex]
                    addPropertyToFilters(title, selectedIndex)
                }
                .setLeftButton(it.getString(R.string.close))
                .build()
                .show()
        }
    }
}