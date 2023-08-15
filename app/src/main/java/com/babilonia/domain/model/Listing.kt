package com.babilonia.domain.model

import com.babilonia.EmptyConstants
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.enums.PublishState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

// Created by Anton Yatsenko on 18.06.2019.
data class Listing(
    var id: Long?,
    var listingType: String?,
    var propertyType: String?,
    var price: Long?,
    var description: String?,
    var bathroomsCount: Int?,
    var bedroomsCount: Int?,
    var totalFloorsCount: Int?,
    var floorNumber: Int?,
    var parkingSlotsCount: Int?,
    var parkingForVisitors: Boolean?,
    var area: Int?,
    var builtArea: Int?,
    var petFriendly: Boolean?,
    var locationAttributes: Location = Location.emptyLocation,
    var yearOfConstruction: Int?,
    var primaryImageId: Int?,
    var facilities: List<Facility>?,
    var advancedDetails: List<Facility>?,
    var images: List<ListingImage>?,
    var user: User? = null,
    var contacts: List<Contact>? = null,
    var status: String? = null,
    var isDraft: Boolean = false,
    var isFavourite: Boolean,
    var viewsCount: Int= 0,
    var contactedCount: Int = 0,
    var favoritesCount: Int = 0,
    var adPlan: PaymentPlanKey? = null,
    var publishState: PublishState? = null,
    var publisherRole: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var adPurchasedAt: String? = null,
    var adExpiresAt: String? = null,
    var url: String? = null,
    var urlShared: String? = null,
    var reason: String? = null,
    var priceFinal: Int? = null,
    ) : ClusterItem {
    override fun getSnippet(): String {
        return EmptyConstants.EMPTY_STRING
    }

    override fun getTitle(): String {
        return EmptyConstants.EMPTY_STRING
    }

    override fun getPosition(): LatLng {
        return LatLng(
            locationAttributes.latitude,
            locationAttributes.longitude
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Listing

        if (id != other.id) return false
        if (url != other.url) return false
        if (urlShared != other.urlShared) return false
        if (reason != other.reason) return false
        if (priceFinal != other.priceFinal) return false
        if (listingType != other.listingType) return false
        if (propertyType != other.propertyType) return false
        if (price != other.price) return false
        if (description != other.description) return false
        if (bathroomsCount != other.bathroomsCount) return false
        if (bedroomsCount != other.bedroomsCount) return false
        if (parkingSlotsCount != other.parkingSlotsCount) return false
        if (area != other.area) return false
        if (builtArea != other.builtArea) return false
        if (petFriendly != other.petFriendly) return false
        if (locationAttributes != other.locationAttributes) return false
        if (yearOfConstruction != other.yearOfConstruction) return false
        if (primaryImageId != other.primaryImageId) return false
        if (facilities != other.facilities) return false
        if (advancedDetails != other.advancedDetails) return false
        if (images != other.images) return false
        if (viewsCount != other.viewsCount) return false
        if (favoritesCount != other.favoritesCount) return false
        if (contactedCount != other.contactedCount) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (urlShared?.hashCode() ?: 0)
        result = 31 * result + (reason?.hashCode() ?: 0)
        result = 31 * result + (priceFinal?.hashCode() ?: 0)
        result = 31 * result + (listingType?.hashCode() ?: 0)
        result = 31 * result + (propertyType?.hashCode() ?: 0)
        result = 31 * result + (price ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (bathroomsCount ?: 0)
        result = 31 * result + (bedroomsCount ?: 0)
        result = 31 * result + (parkingSlotsCount ?: 0)
        result = 31 * result + (area ?: 0)
        result = 31 * result + (builtArea ?: 0)
        result = 31 * result + (petFriendly?.hashCode() ?: 0)
        result = 31 * result + locationAttributes.hashCode()
        result = 31 * result + (yearOfConstruction ?: 0)
        result = 31 * result + (primaryImageId ?: 0)
        result = 31 * result + facilities.hashCode()
        result = 31 * result + advancedDetails.hashCode()
        result = 31 * result + images.hashCode()
        result = 31 * result + viewsCount
        result = 31 * result + contactedCount
        result = 31 * result + favoritesCount
        return result.toInt()
    }

    fun getPreviewImageUrl(): String? {
        if (images.isNullOrEmpty()) return null

        var imageUrl: String? = images?.firstOrNull { it.id == primaryImageId }?.url
        if (imageUrl.isNullOrEmpty()) {
            imageUrl = images?.first()?.url
        }

        return imageUrl
    }

    fun setFrom(another: Listing) {
        id = another.id
        url = another.url
        urlShared = another.urlShared
        reason = another.reason
        priceFinal = another.priceFinal
        listingType = another.listingType
        propertyType = another.propertyType
        price = another.price
        description = another.description
        bathroomsCount = another.bathroomsCount
        bedroomsCount = another.bedroomsCount
        parkingSlotsCount = another.parkingSlotsCount
        area = another.area
        builtArea = another.builtArea
        petFriendly = another.petFriendly
        locationAttributes = another.locationAttributes
        yearOfConstruction = another.yearOfConstruction
        primaryImageId = another.primaryImageId
        facilities = another.facilities
        images = another.images
        user = another.user
        contacts = another.contacts
        status = another.status
        isDraft = another.isDraft
        isFavourite = another.isFavourite
        viewsCount = another.viewsCount
        contactedCount = another.contactedCount
        favoritesCount = another.favoritesCount
        adPlan = another.adPlan
        publishState = another.publishState
        publisherRole = another.publisherRole
        createdAt = another.createdAt
        updatedAt = another.updatedAt
        adPurchasedAt = another.adPurchasedAt
        adExpiresAt = another.adExpiresAt
    }
}