package com.babilonia.data.datasource

import com.babilonia.data.db.model.FacilityDto
import com.babilonia.data.db.model.ImageDto
import com.babilonia.data.db.model.ListingDto
import com.babilonia.domain.model.enums.SortType
import io.reactivex.Completable
import io.reactivex.Single

// Created by Anton Yatsenko on 07.06.2019.
interface ListingsDataSourceLocal {
    fun createListing(listingDto: ListingDto): Single<ListingDto>
    fun deleteListingById(id: Long): Completable
    fun getFacilitiesByType(type: String): Single<List<FacilityDto>>
    fun getAdvancedDetailsByType(type: String): Single<List<FacilityDto>>
    fun getListingById(id: Long): Single<ListingDto>
    fun saveFacilities(type: String, data: List<FacilityDto>): Completable
    fun saveAdvancedDetails(type: String, data: List<FacilityDto>): Completable
    fun uploadListingImage(path: String): Single<ImageDto>
    fun getMyListings(): Single<List<ListingDto>>
    fun saveListings(listings: List<ListingDto>): Completable
    fun getDraftListings(): Single<List<ListingDto>>
    fun getFavouriteListings(): Single<List<ListingDto>>
    fun saveFavourites(listings: List<ListingDto>): Completable
    fun updateFavorite(id: Long, isFavourite: Boolean): Completable
    fun getListings(sortType: SortType): Single<List<ListingDto>>
    fun deleteAll(): Completable
    fun updateListing(listingDto: ListingDto): Single<ListingDto>
}