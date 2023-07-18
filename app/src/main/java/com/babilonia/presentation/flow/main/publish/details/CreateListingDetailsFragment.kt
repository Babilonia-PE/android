package com.babilonia.presentation.flow.main.publish.details

import androidx.lifecycle.Observer
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.databinding.FragmentCreateListingDetailsBinding
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.isVisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.flow.main.search.model.FiltersVisibility
import com.babilonia.presentation.view.numberpicker.ClickNumberPickerListener
import com.babilonia.presentation.view.numberpicker.PickerClickType
import java.util.*

class CreateListingDetailsFragment : BaseCreateListingFragment<FragmentCreateListingDetailsBinding, CreateListingContainerViewModel>() {
    companion object {
        fun newInstance() = CreateListingDetailsFragment()
    }

    override fun viewCreated() {
        binding.model = sharedViewModel
        subscribeToPropertyChange()
        initPickers()
    }

    private fun subscribeToPropertyChange() {
        sharedViewModel.property.observe(this, Observer {
            resetListing()
            showViewsForPropertyType(it)
            fillEmptyStateData()
        })
        sharedViewModel.draftSetEvent.observe(this, Observer {
            setDataFromViewModel()
        })
        sharedViewModel.listing.observe(this, Observer {
            val filtersVisibility = FiltersVisibility
                .getVisibilityByPropertyName(sharedViewModel.property.value?.toLowerCase(Locale.US))
            shouldShowPetFriendly(filtersVisibility)
        })
    }

    private fun shouldShowPetFriendly(filtersVisibility: FiltersVisibility) {
        if (sharedViewModel.listing.value == Constants.RENT && filtersVisibility.isPetFriendlyVisible) {
            binding.cfPetFriendly.visible()
        } else {
            binding.cfPetFriendly.invisible()
        }
    }

    override fun onStop() {
        super.onStop()
        sharedViewModel.draftSetEvent.removeObservers(this)
    }

    override fun onResume() {
        super.onResume()
        setDataFromViewModel()
    }

    private fun initPickers() {

        binding.pickParkingSlots.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType) {
                sharedViewModel.parking.value = currentValue
            }
        })
        binding.pickBedrooms.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType) {
                sharedViewModel.bedroom.value = currentValue
            }
        })
        binding.pickBathrooms.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType) {
                sharedViewModel.bathroom.value = currentValue
            }
        })
        binding.pickTotalFloors.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType) {
                sharedViewModel.totalFloors.value = currentValue
            }
        })
        binding.pickFloorNumber.setClickNumberPickerListener(object : ClickNumberPickerListener {
            override fun onValueChange(previousValue: Int, currentValue: Int, pickerClickType: PickerClickType) {
                sharedViewModel.floorNumber.value = currentValue
            }
        })
        binding.cfParkingForVisitors.setOnCheckChangedCallback {
            sharedViewModel.parkingForVisitors.value = it
        }
        binding.cfPetFriendly.setOnCheckChangedCallback {
            sharedViewModel.petFriendly.value = it
        }

        setDataFromViewModel()
    }

    private fun setDataFromViewModel() {
        binding.pickBathrooms.setPickerValue(sharedViewModel.bathroom.value ?: 0)
        binding.pickBedrooms.setPickerValue(sharedViewModel.bedroom.value ?: 0)
        binding.pickParkingSlots.setPickerValue(sharedViewModel.parking.value ?: 0)
        binding.pickTotalFloors.setPickerValue(sharedViewModel.totalFloors.value ?: 0)
        binding.pickFloorNumber.setPickerValue(sharedViewModel.floorNumber.value ?: 0)
        binding.cfParkingForVisitors.setChecked(sharedViewModel.parkingForVisitors.value ?: false)
        binding.cfPetFriendly.setChecked(sharedViewModel.petFriendly.value ?: false)
    }

    private fun showViewsForPropertyType(it: String?) {
        val filtersVisibility = FiltersVisibility.getVisibilityByPropertyName(it?.toLowerCase(Locale.US))
        binding.pickBathrooms.visibleOrGone(filtersVisibility.isBathroomsVisible)
        binding.pickBedrooms.visibleOrGone(filtersVisibility.isBedroomsVisible)
        binding.pickParkingSlots.visibleOrGone(filtersVisibility.isParkingVisible)
        binding.pickTotalFloors.visibleOrGone(filtersVisibility.isTotalFloorsVisible)
        binding.pickFloorNumber.visibleOrGone(filtersVisibility.isFloorNumberVisible)
        binding.cfParkingForVisitors.visibleOrGone(filtersVisibility.isParkingForVisitorsVisible)
        shouldShowPetFriendly(filtersVisibility)
        shouldShowEmptyState(filtersVisibility)
    }

    private fun shouldShowEmptyState(filtersVisibility: FiltersVisibility) {
        with (filtersVisibility) {
            if (
                isBathroomsVisible ||
                isBedroomsVisible ||
                isParkingVisible ||
                isTotalFloorsVisible ||
                isFloorNumberVisible ||
                isParkingForVisitorsVisible ||
                binding.cfPetFriendly.isVisible()
            ) {
                binding.tvEmptyState.invisible()
            } else {
                binding.tvEmptyState.visible()
            }
        }
    }

    private fun resetListing() {
        if (sharedViewModel.isDraft.not()) {
            sharedViewModel.bathroom.value = 1
            sharedViewModel.bedroom.value = 1
            sharedViewModel.parking.value = 0
            sharedViewModel.totalFloors.value = 0
            sharedViewModel.floorNumber.value = 0
            sharedViewModel.parkingForVisitors.value = false
            sharedViewModel.petFriendly.value = false
        }
    }

    private fun fillEmptyStateData() {
        context?.resources?.let {
            val localizedPropertyType = PropertyType.getLocalizedPropertyName(it, sharedViewModel.property.value)
            binding.tvEmptyState.text = getString(R.string.no_general_details_hint, localizedPropertyType)
        }
    }
}
