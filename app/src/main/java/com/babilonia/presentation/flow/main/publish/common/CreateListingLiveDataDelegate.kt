package com.babilonia.presentation.flow.main.publish.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.ListingImage
import com.babilonia.domain.model.Location
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode

// Created by Anton Yatsenko on 18.06.2019.
interface CreateListingLivedataDelegate {
    var id: Long?
    var isDraft: Boolean
    var description: MutableLiveData<String>
    var location: MutableLiveData<Location>
    var area: MutableLiveData<String>
    var builtArea: MutableLiveData<String>
    var property: MutableLiveData<String>
    var listing: MutableLiveData<String>
    var price: MutableLiveData<String>
    var images: MutableLiveData<List<ListingImage>>
    var bathroom: MutableLiveData<Int>
    var bedroom: MutableLiveData<Int>
    var totalFloors: MutableLiveData<Int>
    var floorNumber: MutableLiveData<Int>
    var parking: MutableLiveData<Int>
    var parkingForVisitors: MutableLiveData<Boolean>
    var petFriendly: MutableLiveData<Boolean>
    var year: MutableLiveData<String>
    var facilities: MutableLiveData<MutableList<Facility>>
    var advancedDetails: MutableLiveData<MutableList<Facility>>

    var propertySelectedEvent: SingleLiveEvent<String>
    var listingChangedEvent: SingleLiveEvent<String>
    val draftSetEvent: SingleLiveEvent<Boolean>
    var editListingImagesEvent: SingleLiveEvent<List<ListingImage>>
    var mediator: MediatorLiveData<Boolean>


    var descriptionTrigger: LiveData<Boolean>
    var areaTrigger: LiveData<Boolean>
    var locationTrigger: LiveData<Boolean>
    var propertyTrigger: LiveData<Boolean>
    var priceTrigger: LiveData<Boolean>
    var yearTrigger: LiveData<Boolean>
    var imagesTrigger: LiveData<Boolean>
    val triggersObserver: Observer<Boolean>

    var createdAt: String?

    fun primaryImageId(): Int?
    fun imageIds(): List<Int>?

    fun setDraft(
        listing: Listing,
        mode: NewListingOpenMode
    )

    fun setFacilities(facilities: List<Facility>)
    fun setAdvancedDetails(advancedDetails: List<Facility>)
}