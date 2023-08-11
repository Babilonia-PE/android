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
import com.babilonia.presentation.flow.ar.ArSceneViewModel
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel
import com.babilonia.presentation.utils.PriceFormatter
import com.babilonia.presentation.view.dialog.SingleChoiceAlertDialog
import com.babilonia.presentation.view.priceview.BarEntry
import com.babilonia.presentation.view.priceview.RANGE_BAR_END_VALUE
import com.babilonia.presentation.view.priceview.RANGE_BAR_START_VALUE
import kotlinx.android.synthetic.main.item_range_bar.view.*
import kotlinx.android.synthetic.main.layout_filters_general.view.*

class ArFiltersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var viewModel: ArSceneViewModel

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filters_ar, this, true)
    }

    fun initGeneralFilters(viewModel: ArSceneViewModel) {
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
//            viewModel.getFacilities(Constants.ALL_FACILITIES)
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
//            viewModel.getFacilities(requestPropertyName)
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

    private fun initToggleButtons() {
        btRent.setOnClickListener {
            setRentChecked()
            viewModel.metadataUpdateSubject.onNext(true)
        }
        btSale.setOnClickListener {
            setSaleChecked()
            viewModel.metadataUpdateSubject.onNext(true)
        }
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