package com.babilonia.data.network.model.json

import com.babilonia.Constants
import com.google.gson.annotations.SerializedName
import java.util.*

// Created by Anton Yatsenko on 11.06.2019.
open class ListingJson {

    @SerializedName("id")
    var id: Long = UUID.randomUUID().hashCode().toLong()

    @SerializedName("url")
    var url: UrlJson? = null

    @SerializedName("listing_type")
    var listingType: String? = null

    @SerializedName("property_type")
    var propertyType: String? = null

    @SerializedName("price")
    var price: Long? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("bathrooms_count")
    var bathroomsCount: Int? = null

    @SerializedName("bedrooms_count")
    var bedroomsCount: Int? = null

    @SerializedName("total_floors_count")
    var totalFloorsCount: Int? = null

    @SerializedName("floor_number")
    var floorNumber: Int? = null

    @SerializedName("parking_slots_count")
    var parkingSlotsCount: Int? = null

    @SerializedName("parking_for_visits")
    var parkingForVisitors: Boolean? = null

    @SerializedName("area")
    var area: Int? = null

    @SerializedName("built_area")
    var builtArea: Int? = null

    @SerializedName("pet_friendly")
    var petFriendly: Boolean? = null

    @SerializedName("status")
    var status: String? = Constants.HIDDEN

    @SerializedName("facility_ids")
    var facilityIds: List<Int> = mutableListOf()

    @SerializedName("advanced_detail_ids")
    var advancedDetailsIds: List<Int> = mutableListOf()

    @SerializedName("location_attributes", alternate = ["location"])
    var locationAttributes: LocationJson? = null

    @SerializedName("year_of_construction")
    var yearOfConstruction: Int? = null

    @SerializedName("image_ids")
    var imageIds: List<Int> = mutableListOf()

    @SerializedName("primary_image_id")
    var primaryImageId: Int? = null

    @SerializedName("user")
    var user: UserJson? = null

    @SerializedName("contact")
    var contact: ContactJson? = null

    @SerializedName("facilities")
    var facilities: List<FacilityJson> = mutableListOf()

    @SerializedName("advanced_details")
    var advancedDetails: List<FacilityJson> = mutableListOf()

    @SerializedName("images")
    var images: List<ImageJson> = mutableListOf()

    @SerializedName("favourited")
    var favourited: Boolean = false

    @SerializedName("views_count")
    var viewsCount: Int = 0

    @SerializedName("contacts_count")
    var contactedCount: Int = 0

    @SerializedName("favourites_count")
    var favoritesCount: Int = 0

    @SerializedName("ad_plan")
    var adPlan: String? = null

    @SerializedName("state")
    var publishState: String? = null

    @SerializedName("publisher_role")
    var publisherRole: String? = null

    @SerializedName("created_at")
    var createdAt: String? = null

    @SerializedName("updated_at")
    var updatedAt: String? = null

    @SerializedName("ad_expires_at")
    var adExpiresAt: String? = null

    @SerializedName("ad_purchased_at")
    var adPurchasedAt: String? = null
}