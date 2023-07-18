package com.babilonia.data.mapper

import com.babilonia.Constants
import com.babilonia.data.db.model.FacilityDto
import com.babilonia.data.db.model.ImageDto
import com.babilonia.data.db.model.ListingDto
import com.babilonia.data.network.model.json.FacilityJson
import com.babilonia.data.network.model.json.ImageJson
import com.babilonia.data.network.model.json.ListingJson
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.enums.PublishState
import io.realm.RealmList
import javax.inject.Inject

// Created by Anton Yatsenko on 20.06.2019.
class ListingMapper @Inject constructor(
    private val imageMapper: ListingImageMapper,
    private val facilityMapper: FacilityMapper,
    private val userMapper: UserMapper,
    private val locationMapper: LocationMapper
) : Mapper<ListingDto, ListingJson, Listing> {

    override fun mapRemoteToLocal(from: ListingJson): ListingDto {
        return mapDomainToLocal(mapRemoteToDomain(from))
    }

    override fun mapLocalToRemote(from: ListingDto): ListingJson {
        return mapDomainToRemote(mapLocalToDomain(from))
    }

    override fun mapDomainToLocal(from: Listing): ListingDto {
        return ListingDto().apply {
            id = from.id ?: 0
            listingType = from.listingType
            propertyType = from.propertyType
            price = from.price
            description = from.description
            bedroomsCount = from.bedroomsCount
            bathroomsCount = from.bathroomsCount
            totalFloorsCount = from.totalFloorsCount
            floorNumber = from.floorNumber
            parkingSlotsCount = from.parkingSlotsCount
            parkingForVisitors = from.parkingForVisitors
            area = from.area
            builtArea = from.builtArea
            locationAttributes = from.locationAttributes.let { locationMapper.mapDomainToLocal(it) }
            petFriendly = from.petFriendly
            yearOfConstruction = from.yearOfConstruction
            primaryImageId = from.primaryImageId

            val realmFacilities = RealmList<FacilityDto>()
            from.facilities?.let {
                realmFacilities.addAll(it.map { facilityMapper.mapDomainToLocal(it) })
                facilityIds.addAll(it.map { it.id })
            }
            facilities = realmFacilities

            val realmAdvancedDetails = RealmList<FacilityDto>()
            from.advancedDetails?.let {
                realmAdvancedDetails.addAll(it.map { facilityMapper.mapDomainToLocal(it) })
                advancedDetailsIds.addAll(it.map { it.id })
            }
            advancedDetails = realmAdvancedDetails

            val imagesRealm = RealmList<ImageDto>()
            from.images?.let {
                imagesRealm.addAll(it.map { imageMapper.mapDomainToLocal(it) })
                imageIds.addAll(it.map { it.id })
            }
            images = imagesRealm
            user = from.user?.let { userMapper.mapDomainToLocal(it) }
            draft = from.isDraft
            status = from.status
            favourited = from.isFavourite
            viewsCount = from.viewsCount
            contactedCount = from.contactedCount
            favoritesCount = from.favoritesCount
            adPlan = from.adPlan?.name?.toLowerCase()
            publishState = from.publishState?.name?.toLowerCase()
            createdAt = from.createdAt
            updatedAt = from.updatedAt
            adPurchasedAt = from.adPurchasedAt
            adExpiresAt = from.adExpiresAt
        }
    }

    override fun mapDomainToRemote(from: Listing): ListingJson {
        return ListingJson().apply {
            id = from.id ?: 0
            listingType = from.listingType
            propertyType = from.propertyType
            price = from.price
            description = from.description
            bedroomsCount = from.bedroomsCount
            bathroomsCount = from.bathroomsCount
            totalFloorsCount = from.totalFloorsCount
            floorNumber = from.floorNumber
            parkingSlotsCount = from.parkingSlotsCount
            parkingForVisitors = from.parkingForVisitors
            area = from.area
            builtArea = from.builtArea
            locationAttributes = from.locationAttributes.let { locationMapper.mapDomainToRemote(it) }
            petFriendly = from.petFriendly
            yearOfConstruction = from.yearOfConstruction
            primaryImageId = from.primaryImageId
            val realmFacilities = RealmList<FacilityJson>()
            from.facilities?.let {
                realmFacilities.addAll(it.map { facilityMapper.mapDomainToRemote(it) })
                facilityIds = it.map { it.id }
            }
            facilities = realmFacilities
            val realmAdvancedDetails = RealmList<FacilityJson>()
            from.advancedDetails?.let {
                realmAdvancedDetails.addAll(it.map { facilityMapper.mapDomainToRemote(it) })
                advancedDetailsIds = it.map { it.id }
            }
            advancedDetails = realmAdvancedDetails
            val imagesRealm = RealmList<ImageJson>()
            from.images?.let {
                imagesRealm.addAll(it.map { imageMapper.mapDomainToRemote(it) })
                imageIds = it.map { it.id }
            }
            user = from.user?.let { userMapper.mapDomainToRemote(it) }
            status = from.status
            favourited = from.isFavourite
            viewsCount = from.viewsCount
            contactedCount = from.contactedCount
            favoritesCount = from.favoritesCount
        }
    }

    override fun mapLocalToDomain(from: ListingDto): Listing {
        val facilities = from.facilities.map { facilityMapper.mapLocalToDomain(it) }
        for (i in facilities.indices) {
            facilities[i].isChecked = from.facilityIds.contains(facilities[i].id)
        }
        val advancedDetails = from.advancedDetails.map { facilityMapper.mapLocalToDomain(it) }
        for (i in advancedDetails.indices) {
            advancedDetails[i].isChecked = from.advancedDetailsIds.contains(advancedDetails[i].id)
        }
        return Listing(
            from.id,
            from.listingType,
            from.propertyType,
            from.price,
            from.description,
            from.bathroomsCount,
            from.bedroomsCount,
            from.totalFloorsCount,
            from.floorNumber,
            from.parkingSlotsCount,
            from.parkingForVisitors,
            from.area,
            from.builtArea,
            from.petFriendly,
            from.locationAttributes?.let { locationMapper.mapLocalToDomain(it) } ?: Location.emptyLocation,
            from.yearOfConstruction,
            from.primaryImageId,
            facilities,
            advancedDetails,
            from.images.map {
                imageMapper.mapLocalToDomain(it).apply {
                    if (id == from.primaryImageId) {
                        primary = true
                    }
                }
            }.sortedByDescending { it.id == from.primaryImageId },
            from.user?.let { userMapper.mapLocalToDomain(it) },
            from.status ?: Constants.HIDDEN,
            from.draft,
            from.favourited,
            from.viewsCount,
            from.contactedCount,
            from.favoritesCount,
            from.adPlan?.let { PaymentPlanKey.valueOf(it.toUpperCase()) },
            from.publishState?.let { PublishState.valueOf(it.toUpperCase()) },
            from.createdAt,
            from.updatedAt,
            from.adPurchasedAt,
            from.adExpiresAt
        )
    }

    override fun mapRemoteToDomain(from: ListingJson): Listing {
        return Listing(
            from.id,
            from.listingType,
            from.propertyType,
            from.price,
            from.description,
            from.bathroomsCount,
            from.bedroomsCount,
            from.totalFloorsCount,
            from.floorNumber,
            from.parkingSlotsCount,
            from.parkingForVisitors,
            from.area,
            from.builtArea,
            from.petFriendly,
            from.locationAttributes?.let { locationMapper.mapRemoteToDomain(it) } ?: Location.emptyLocation,
            from.yearOfConstruction,
            from.primaryImageId,
            from.facilities.map { facilityMapper.mapRemoteToDomain(it) },
            from.advancedDetails.map { facilityMapper.mapRemoteToDomain(it) },
            from.images.map {
                imageMapper.mapRemoteToDomain(it).apply {
                    if (id == from.primaryImageId) {
                        primary = true
                    }
                }
            }.sortedByDescending { it.id == from.primaryImageId },
            from.user?.let { userMapper.mapRemoteToDomain(it) },
            from.status ?: Constants.HIDDEN,
            false,
            from.favourited,
            from.viewsCount,
            from.contactedCount,
            from.favoritesCount,
            from.adPlan?.let { PaymentPlanKey.valueOf(it.toUpperCase()) },
            from.publishState?.let { PublishState.valueOf(it.toUpperCase()) },
            from.createdAt,
            from.updatedAt,
            from.adPurchasedAt,
            from.adExpiresAt
        )
    }
}