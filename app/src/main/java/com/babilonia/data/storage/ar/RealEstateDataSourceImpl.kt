package com.babilonia.data.storage.ar

import com.babilonia.Constants
import com.babilonia.data.datasource.RealEstateDataSource
import com.babilonia.data.mapper.ListingMapper
import com.babilonia.data.network.model.json.ListingJson
import com.babilonia.data.network.service.ListingsService
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.ListingImage
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.geo.ILocation
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class RealEstateDataSourceImpl @Inject constructor(
    private val listingsService: ListingsService,
    private val listingsMapper: ListingMapper
) : RealEstateDataSource {

    fun getListings(
        lat: Float,
        lon: Float,
        radius: Int
    ): Single<List<ListingJson>> {
        return listingsService.getArListings(
            lat,
            lon,
            radius
        ).map { it.data.records }
    }

    override fun getRealEstateList(currentLocation: ILocation): Observable<List<Listing>> {
        return getListings(currentLocation.latitude.toFloat(), currentLocation.longitude.toFloat(), Constants.AR_PRELOAD_DISTANCE)
            .flatMapObservable { listings ->
                Observable.just(listings.map { listingsMapper.mapRemoteToDomain(it) })
            }
    }

    private val resultList = listOf(
        Listing(
            id = 1,
            listingType = Constants.RENT,
            propertyType = Constants.APARTMENT,
            price = 60_000,
            description = null,
            bathroomsCount = 1,
            bedroomsCount = 3,
            parkingSlotsCount = 0,
            area = 80,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = 48.451848,
                longitude = 35.068903,
                address = "Building 6"
            ),
            yearOfConstruction = 1980,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://cdn.zeplin.io/5ce276ca10cfed2a8fe21bc9/assets/769B95C3-FE08-4F4A-90A4-CC8A4CA84CA2.png",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 2,
            listingType = Constants.RENT,
            propertyType = Constants.APARTMENT,
            price = 750_500,
            description = null,
            bathroomsCount = 1,
            bedroomsCount = 2,
            parkingSlotsCount = 0,
            area = 58,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = 48.4525191,
                longitude = 35.0703094,
                address = "Building 8"
            ),
            yearOfConstruction = 1980,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://cdn.zeplin.io/5ce276ca10cfed2a8fe21bc9/assets/B20B19F3-32C9-42F7-95E1-0F59C2E3AB6E.png",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 3,
            listingType = Constants.SALE,
            propertyType = Constants.COMMMERCIAL,
            price = 1_500_500,
            description = null,
            bathroomsCount = 0,
            bedroomsCount = 0,
            parkingSlotsCount = 50,
            area = 1280,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = 48.452000,
                longitude = 35.071202,
                address = "Biggest building"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://cdn.zeplin.io/5ce276ca10cfed2a8fe21bc9/assets/2208765D-D67D-4CE7-AC02-00BACB9CC1AB.png",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 4,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 59_500,
            description = null,
            bathroomsCount = 1,
            bedroomsCount = 3,
            parkingSlotsCount = 0,
            area = 74,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = 48.443849,
                longitude = 35.0247332,
                address = "Some city, Some street, build 1"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/1070945/pexels-photo-1070945.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 5,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 159_700,
            description = null,
            bathroomsCount = 2,
            bedroomsCount = 5,
            parkingSlotsCount = 0,
            area = 124,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = 48.4431249,
                longitude = 35.0240546,
                address = "Some city, Some street, build 2"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/1034662/pexels-photo-1034662.jpeg",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 6,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 77_300,
            description = null,
            bathroomsCount = 2,
            bedroomsCount = 4,
            parkingSlotsCount = 0,
            area = 97,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = 48.4439002,
                longitude = 35.0217158,
                address = "Some city, Some street, build 3"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/548084/pexels-photo-548084.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        //Lima objects
        Listing(
            id = 7,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 59_500,
            description = null,
            bathroomsCount = 1,
            bedroomsCount = 3,
            parkingSlotsCount = 0,
            area = 74,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = -12.116331,
                longitude = -77.0414429,
                address = "Some city, Some street, build 1"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/1070945/pexels-photo-1070945.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 8,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 159_700,
            description = null,
            bathroomsCount = 2,
            bedroomsCount = 5,
            parkingSlotsCount = 0,
            area = 124,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = -12.1164306,
                longitude = -77.043645,
                address = "Some city, Some street, build 2"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/1034662/pexels-photo-1034662.jpeg",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 9,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 77_300,
            description = null,
            bathroomsCount = 2,
            bedroomsCount = 4,
            parkingSlotsCount = 0,
            area = 97,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = -12.1166162,
                longitude = -77.0446421,
                address = "Some city, Some street, build 3"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/548084/pexels-photo-548084.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        ),
        Listing(
            id = 10,
            listingType = Constants.SALE,
            propertyType = Constants.APARTMENT,
            price = 33_800,
            description = null,
            bathroomsCount = 2,
            bedroomsCount = 4,
            parkingSlotsCount = 0,
            area = 97,
            builtArea = 0,
            petFriendly = false,
            locationAttributes = Location(
                latitude = -12.1163028,
                longitude = -77.042845,
                address = "Some city, Some street, build 3"
            ),
            yearOfConstruction = 2010,
            primaryImageId = 1,
            facilities = emptyList(),
            images = listOf(
                ListingImage(
                    url = "https://images.pexels.com/photos/1070945/pexels-photo-1070945.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
                    primary = true,
                    id = 1
                )
            ),
            user = null,
            status = "",
            isDraft = false,
            isFavourite = false,
            totalFloorsCount = 0,
            floorNumber = 0,
            parkingForVisitors = false,
            advancedDetails = emptyList()
        )
    )
}
