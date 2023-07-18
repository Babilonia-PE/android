package com.babilonia.presentation.flow.main.publish.createlisting

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.Location
import com.babilonia.presentation.extension.formattedStringToInt
import com.babilonia.presentation.flow.main.publish.common.CreateListingLivedataDelegate
import com.babilonia.presentation.flow.main.publish.common.CreateListingLivedataImpl
import com.babilonia.presentation.flow.main.publish.common.ListingPage
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import com.babilonia.presentation.flow.main.search.model.FiltersVisibility
import com.babilonia.presentation.utils.DateFormatter
import org.joda.time.DateTime
import java.util.*

// Created by Anton Yatsenko on 31.05.2019.
class CreateListingSharedViewModel : ViewModel(),
    CreateListingLivedataDelegate by CreateListingLivedataImpl() {


    var currentPage: ListingPage = ListingPage.COMMON
    var currentItem = 0
    var mode = NewListingOpenMode.NEW
    var status: String = Constants.HIDDEN
    var shouldEnableContinueButton = object : ObservableBoolean() {
        override fun get(): Boolean {
            return when (currentPage) {
                ListingPage.COMMON -> validateCommonPage()
                ListingPage.DETAILS -> validateDetailsPage()
                ListingPage.FACILITIES -> true
                ListingPage.ADVANCED -> true
                ListingPage.PHOTOS -> validateForImagesPage()
            }
        }
    }

    fun onFacilityChange(value: Facility?) {
        value?.also {
            facilities.value?.remove(it)
            facilities.value?.add(it)
        }
    }

    fun onAdvancedDetailChange(value: Facility?) {
        value?.also {
            advancedDetails.value?.remove(it)
            advancedDetails.value?.add(it)
        }
    }

    fun fillWithDefaultValues() {
        property.value = Constants.APARTMENT
        listing.value = Constants.SALE
        bathroom.value = 1
        bedroom.value = 1
        parking.value = 0
        petFriendly.value = true
        propertySelectedEvent.postValue(property.value)
        listingChangedEvent.postValue(listing.value)
        createdAt = DateFormatter.toResponseDate(DateTime.now())
    }

    fun mapLiveDataToParams(): Listing {
        val locationDto: Location = if (location.value != null) {
            Location().apply {
                longitude = location.value?.longitude ?: EmptyConstants.ZERO_DOUBLE
                latitude = location.value?.latitude ?: EmptyConstants.ZERO_DOUBLE
                address = location.value?.address
            }
        } else {
            Location.emptyLocation
        }
        resetValuesForPropertyType()
        return Listing(
            id,
            listing.value,
            property.value?.toLowerCase(Locale.getDefault()),
            price.value?.formattedStringToInt(),
            description.value,
            bathroom.value,
            bedroom.value,
            totalFloors.value,
            floorNumber.value,
            parking.value,
            parkingForVisitors.value,
            area.value?.formattedStringToInt(),
            builtArea.value?.formattedStringToInt(),
            petFriendly.value,
            locationDto,
            year.value?.toInt(),
            primaryImageId(),
            facilities.value?.filter { it.isChecked },
            advancedDetails.value?.filter { it.isChecked },
            images.value,
            null,
            status,
            isDraft = true,
            isFavourite = false
        ).apply {
            this@CreateListingSharedViewModel.createdAt?.let { createdAt = it }
        }
    }

    private fun resetValuesForPropertyType() {
        val filtersVisibility = FiltersVisibility.getVisibilityByPropertyName(property.value?.toLowerCase(Locale.US))
        with (filtersVisibility) {
            if (!isTotalAreaVisible) area.value = null
            if (!isBuiltAreaVisible) builtArea.value = null
            if (!isYearOfConstructionVisible) year.value = null
            if (!isBathroomsVisible) bathroom.value = null
            if (!isBedroomsVisible) bedroom.value = null
            if (!isTotalFloorsVisible) totalFloors.value = null
            if (!isFloorNumberVisible) floorNumber.value = null
            if (!isParkingVisible) parking.value = null
            if (!isParkingForVisitorsVisible) parkingForVisitors.value = null
            if (!isPetFriendlyVisible && listing.value != Constants.RENT) petFriendly.value = null
        }
    }

    fun validatePage() {
        shouldEnableContinueButton.notifyChange()
    }

    private fun validateCommonPage(): Boolean {
        val filtersVisibility = FiltersVisibility.getVisibilityByPropertyName(property.value?.toLowerCase(Locale.US))
        val isYearOk = filtersVisibility.isYearOfConstructionVisible.not() || yearTrigger.value == true
        return (descriptionTrigger.value == true
                && locationTrigger.value == true
                && areaTrigger.value == true
                && propertyTrigger.value == true
                && priceTrigger.value == true
                && isYearOk)
    }

    private fun validateDetailsPage(): Boolean {
        return true
    }

    private fun validateForImagesPage(): Boolean {
        return imagesTrigger.value == true
    }
}