package com.babilonia.data.db.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

// Created by Anton Yatsenko on 11.06.2019.
open class ListingDto : RealmObject() {
    @PrimaryKey
    var id: Long = UUID.randomUUID().hashCode().toLong()
    var url: String? = null
    var urlShared: String? = null
    var listingType: String? = null
    var propertyType: String? = null
    var price: Long? = null
    var description: String? = null
    var bathroomsCount: Int? = null
    var bedroomsCount: Int? = null
    var priceFinal: Int? = null
    var totalFloorsCount: Int? = null
    var floorNumber: Int? = null
    var parkingSlotsCount: Int? = null
    var parkingForVisitors: Boolean? = null
    var area: Int? = null
    var builtArea: Int? = null
    var petFriendly: Boolean? = null
    var status: String? = null
    var facilityIds: RealmList<Int> = RealmList()
    var advancedDetailsIds: RealmList<Int> = RealmList()
    var locationAttributes: LocationDto? = null
    var yearOfConstruction: Int? = null
    var imageIds: RealmList<Int> = RealmList()
    var primaryImageId: Int? = null
    var user: UserDto? = null
    var contact: ContactDto? = null

    var facilities: RealmList<FacilityDto> = RealmList()
    var advancedDetails: RealmList<FacilityDto> = RealmList()
    var images: RealmList<ImageDto> = RealmList()
    var draft = false
    var favourited: Boolean = false

    var viewsCount = 0
    var contactedCount = 0
    var favoritesCount = 0

    var adPlan: String? = null
    var publishState: String? = null
    var publisherRole: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var adPurchasedAt: String? = null
    var adExpiresAt: String? = null
    var reason: String? = null
}