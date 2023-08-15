package com.babilonia.presentation.flow.main.publish.common

import androidx.lifecycle.*
import com.babilonia.EmptyConstants
import com.babilonia.domain.model.*
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode

// Created by Anton Yatsenko on 18.06.2019.
class CreateListingLivedataImpl : CreateListingLivedataDelegate {
    override var id: Long? = null
    override var isDraft: Boolean = false

    override var description = MutableLiveData<String>()
    override var location = MutableLiveData<Location>()
    override var tempLocation = MutableLiveData<Location>()
    override var area = MutableLiveData<String>()
    override var builtArea = MutableLiveData<String>()
    override var property = MutableLiveData<String>()
    override var listing = MutableLiveData<String>()
    override var price = MutableLiveData<String>()
    override var images = MutableLiveData<List<ListingImage>>()
    override var bathroom = MutableLiveData<Int>()
    override var bedroom = MutableLiveData<Int>()
    override var totalFloors = MutableLiveData<Int>()
    override var floorNumber = MutableLiveData<Int>()
    override var parking = MutableLiveData<Int>()
    override var parkingForVisitors = MutableLiveData<Boolean>()
    override var petFriendly = MutableLiveData<Boolean>()
    override var year = MutableLiveData<String>()
    override var facilities = MutableLiveData<MutableList<Facility>>()
    override var advancedDetails = MutableLiveData<MutableList<Facility>>()

    override var propertySelectedEvent = SingleLiveEvent<String>()
    override var listingChangedEvent: SingleLiveEvent<String> = SingleLiveEvent()
    override val draftSetEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    override var editListingImagesEvent: SingleLiveEvent<List<ListingImage>> = SingleLiveEvent()

    override var mediator = MediatorLiveData<Boolean>()
    private val facilityIds = mutableListOf<Int>()
    private val advancedDetailsIds = mutableListOf<Int>()


    override var descriptionTrigger: LiveData<Boolean> = Transformations.map(description) {
        it.isNullOrEmpty().not()
    }
    override var areaTrigger: LiveData<Boolean> = Transformations.map(area) {
        it.isNullOrEmpty().not()
    }
    override var locationTrigger: LiveData<Boolean> = Transformations.map(location) {
        it != null
    }
    override var propertyTrigger: LiveData<Boolean> = Transformations.map(property) {
        it.isNullOrEmpty().not()
    }
    override var priceTrigger: LiveData<Boolean> = Transformations.map(price) {
        it.isNullOrEmpty().not()
    }
    override var yearTrigger: LiveData<Boolean> = Transformations.map(year) {
        it.isNullOrEmpty().not()
    }
    override var imagesTrigger: LiveData<Boolean> = Transformations.map(images) {
        it.isNullOrEmpty().not()
    }
    override val triggersObserver = Observer<Boolean> {
        mediator.value = it
    }

    override var contact = MutableLiveData<Contact>()

    override var createdAt: String? = null

    init {
        mediator.addSource(descriptionTrigger, triggersObserver)
        mediator.addSource(locationTrigger, triggersObserver)
        mediator.addSource(areaTrigger, triggersObserver)
        mediator.addSource(propertyTrigger, triggersObserver)
        mediator.addSource(priceTrigger, triggersObserver)
        mediator.addSource(imagesTrigger, triggersObserver)
        mediator.addSource(yearTrigger, triggersObserver)
        id = System.currentTimeMillis()
    }

    override fun primaryImageId(): Int? {
        return if (images.value.isNullOrEmpty().not()) {
            images.value?.firstOrNull { it.primary }?.id ?: images.value?.first()?.id
        } else {
            EmptyConstants.EMPTY_INT
        }
    }

    override fun imageIds(): List<Int>? {
        return images.value?.map { it.id }
    }

    override fun setDraft(model: Listing, mode: NewListingOpenMode) {
        isDraft = true
        id = model.id
        description.value = model.description
        location.value = model.locationAttributes
        contact.value = model.contacts?.first()
        area.value = model.area?.toString()
        builtArea.value = model.builtArea?.toString()
        property.value = model.propertyType?.substring(0, 1)?.toUpperCase()?.plus(model.propertyType?.substring(1))
        listing.value = model.listingType
        price.value = model.price?.toString()
        facilities.value = model.facilities?.toMutableList()
        advancedDetails.value = model.advancedDetails?.toMutableList()
        bathroom.value = model.bathroomsCount
        bedroom.value = model.bedroomsCount
        totalFloors.value = model.totalFloorsCount
        floorNumber.value = model.floorNumber
        parking.value = model.parkingSlotsCount
        parkingForVisitors.value = model.parkingForVisitors
        petFriendly.value = model.petFriendly
        year.value = model.yearOfConstruction?.toString()
        propertySelectedEvent.postValue(model.propertyType)
        listingChangedEvent.postValue(listing.value)
        if (mode == NewListingOpenMode.EDIT) {
            images.value = model.images
            editListingImagesEvent.postValue(images.value)
        }
        val ids = model.facilities?.map { it.id }?.toMutableList()
        ids?.let { facilityIds.addAll(it) }
        val advancedIds = model.advancedDetails?.map { it.id }?.toMutableList()
        advancedIds?.let { advancedDetailsIds.addAll(it) }
        draftSetEvent.postValue(true)
    }

    override fun setFacilities(facilities: List<Facility>) {
        for (i in facilities.indices) {
            facilities[i].isChecked = facilityIds.contains(facilities[i].id)
        }
        this.facilities.value = facilities.toMutableList()
    }

    override fun setAdvancedDetails(advancedDetails: List<Facility>) {
        for (i in advancedDetails.indices) {
            advancedDetails[i].isChecked = advancedDetailsIds.contains(advancedDetails[i].id)
        }
        this.advancedDetails.value = advancedDetails.toMutableList()
    }

}