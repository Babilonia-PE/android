package com.babilonia.presentation.flow.main.publish.listingtype


import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.lifecycle.Observer
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.databinding.FragmentListingTypeBinding
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import com.babilonia.presentation.flow.main.search.model.FiltersVisibility
import com.babilonia.presentation.utils.NumberTextWatcher
import com.babilonia.presentation.view.dialog.SingleChoiceAlertDialog
import com.babilonia.presentation.view.picker.NumberPickerDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import java.util.*


private const val PLACE_PICKER_REQUEST = 59

class ListingTypeFragment :
    BaseCreateListingFragment<FragmentListingTypeBinding, CreateListingContainerViewModel>() {
    companion object {
        fun newInstance() = ListingTypeFragment()
    }

    override fun viewCreated() {
        binding.model = sharedViewModel
        if (sharedViewModel.mode != NewListingOpenMode.EDIT) {
            initToggleButtons()
        } else {
            binding.btSale.alpha = 0.5f
            binding.btRent.alpha = 0.5f
            binding.btSale.isClickable = false
            binding.btRent.isClickable = false
            binding.btSale.isFocusable = false
            binding.btRent.isFocusable = false
        }
        initClicks()
        observeViewModel()
        binding.etPrice.addTextChangedListener(NumberTextWatcher(binding.etPrice, "#,###"))
    }

    //Moved initPropertyTypes to onResume because of material dropdown bug
    override fun onResume() {
        super.onResume()
        if (sharedViewModel.mode != NewListingOpenMode.EDIT) {
            initPropertyTypes()
        } else {

            binding.etPropertyType.alpha = 0.5f
        }
    }

    private fun observeViewModel() {
        sharedViewModel.property.observe(this, Observer {
            val localized = PropertyType.getLocalizedPropertyName(resources, it)
            binding.etPropertyType.setText(localized)
            updateFiltersVisibility(it)
        })
        sharedViewModel.listing.observe(this, Observer {
            setPriceHint(it)
        })
        sharedViewModel.listingChangedEvent.observe(this, Observer {
            if (it == Constants.RENT) {
                setRentChecked()
            } else {
                setSaleChecked()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClicks() {
        binding.tiDescription.setEndIconOnClickListener {
            viewModel.navigateToDescription()
        }
        binding.tiAddress.setEndIconOnClickListener {
            requestLocationPermission()
        }
        binding.etAddress.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                requestLocationPermission()
                v.performClick()
            }
            false
        }
        binding.etDescription.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                viewModel.navigateToDescription()
                v.performClick()
            }
            false
        }
        binding.etArea.addTextChangedListener(NumberTextWatcher(binding.etArea, "#,###"))
        binding.etBuiltArea.addTextChangedListener(NumberTextWatcher(binding.etBuiltArea, "#,###"))
        binding.etYear.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showYearDialog()
            }
            false
        }}

    @SuppressLint("CheckResult")
    private fun requestLocationPermission() {
        RxPermissions(this).request(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                if (it) {
                    viewModel.navigateToPlacePicker()
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPropertyTypes() {
        binding.etPropertyType.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showPropertyDialog()
            }
            false
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showPropertyDialog() {
        val array = resources.getStringArray(R.array.property_types)
        val currentPropertyType = binding.etPropertyType.text.toString()
        var selectedIndex = array.indexOf(currentPropertyType)

        context?.let {
            SingleChoiceAlertDialog.Builder(it)
                .setTitleText(getString(R.string.property_type))
                .setSingleChoiceItems(array, selectedIndex) { which ->
                    selectedIndex = which
                }
                .setRightButton(getString(R.string.ok)) {
                    sharedViewModel.facilities.value?.clear()
                    val unlocalizedPropertyName = PropertyType.getPropertyName(selectedIndex).capitalize()
                    sharedViewModel.propertySelectedEvent.postValue(unlocalizedPropertyName)
                    sharedViewModel.property.postValue(unlocalizedPropertyName)
                    binding.etPropertyType.setText(array[selectedIndex])
                    updateFiltersVisibility(unlocalizedPropertyName)
                }
                .setLeftButton(getString(R.string.close))
                .build()
                .show()
        }
    }

    private fun updateFiltersVisibility(unlocalizedPropertyName: String) {
        val filtersVisibility = FiltersVisibility.getVisibilityByPropertyName(unlocalizedPropertyName.toLowerCase(Locale.US))
        binding.tiBuiltArea.visibleOrGone(filtersVisibility.isBuiltAreaVisible)
        binding.tiYear.visibleOrGone(filtersVisibility.isYearOfConstructionVisible)
    }

    private fun showYearDialog() {
        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val selectedYear = try {
            sharedViewModel.year.value?.toInt() ?: currentYear
        } catch (ignored: Exception) {
            currentYear
        }
        NumberPickerDialog(
            requireContext(),
            Constants.MIN_YEAR,
            currentYear,
            selectedYear,
            object : NumberPickerDialog.NumberPickerCallBack {
                override fun onSelectingValue(value: Int) {
                    binding.etYear.setText(value.toString())
                }

            }).show()
    }

    private fun initToggleButtons() {
        binding.btRent.setOnClickListener {
            setRentChecked()
        }
        binding.btSale.setOnClickListener {
            setSaleChecked()
        }
    }

    private fun setRentChecked() {
        sharedViewModel.listing.value = Constants.RENT
        binding.btSale.isEnabled = true
        binding.btSale.isChecked = false
        setPriceHint(Constants.RENT)
    }

    private fun setSaleChecked() {
        sharedViewModel.listing.value = Constants.SALE
        binding.btRent.isEnabled = true
        binding.btRent.isChecked = false
        setPriceHint(Constants.SALE)
    }

    private fun setPriceHint(listingType: String) {
        when (listingType) {
            Constants.SALE -> binding.tyPrice.hint = getString(R.string.price_for_sale)
            Constants.RENT -> binding.tyPrice.hint = getString(R.string.price_for_rent)
        }
    }
}
