package com.babilonia.data.repository

import com.babilonia.Constants
import com.babilonia.data.datasource.AuthDataSourceLocal
import com.babilonia.data.datasource.ListingsDataSourceLocal
import com.babilonia.data.datasource.ListingsDataSourceRemote
import com.babilonia.data.db.model.ListingDto
import com.babilonia.data.db.model.UserDto
import com.babilonia.data.di.LOCAL
import com.babilonia.data.di.MEMORY
import com.babilonia.data.mapper.*
import com.babilonia.data.network.error.mapErrors
import com.babilonia.data.network.error.mapNetworkErrors
import com.babilonia.data.network.model.json.ListingJson
import com.babilonia.domain.model.*
import com.babilonia.domain.model.enums.FacilityDataType
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.SortType
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import com.babilonia.presentation.view.priceview.BarEntry
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*
import javax.inject.Inject
import javax.inject.Named

// Created by Anton Yatsenko on 07.06.2019.
class ListingRepositoryImpl @Inject constructor(
    @Named(LOCAL) private val listingDataSourceLocal: ListingsDataSourceLocal,
    @Named(MEMORY) private val listingDataSourceMemory: ListingsDataSourceLocal,
    private val listingDataSourceRemote: ListingsDataSourceRemote,
    private val authDataSourceLocal: AuthDataSourceLocal,
    private val listingImageMapper: ListingImageMapper,
    private val facilityMapper: FacilityMapper,
    private val listingMapper: ListingMapper,
    private val filtersMapper: FiltersMapper,
    private val recentSearchMapper: RecentSearchMapper,
    private val listingsMetadataMapper: ListingsMetadataMapper,
    private val dataLocationMapper: DataLocationMapper
) : ListingRepository {
    override fun getListingsPriceRange(
        lat: Float,
        lon: Float,
        radius: Int,
        filters: List<Filter>,
        facilities: List<Facility>
    ): Single<List<BarEntry>> {
        return listingDataSourceRemote
            .getListingsPriceRange(
                lat,
                lon,
                radius,
                filtersMapper.mapToHistogramQuery(filters),
                filtersMapper.mapFacilitiesToQuery(facilities)
            )
            .map { range -> range.map { BarEntry(it.number, it.listingsCount, it.startPrice) } }
    }

    override fun getListingsMetadata(
        lat: Float?,
        lon: Float?,
        radius: Int?,
        filters: List<Filter>,
        facilities: List<Facility>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Observable<ListingsMetadata> {
        return listingDataSourceRemote
            .getListingsMetadata(
                lat,
                lon,
                radius,
                filtersMapper.mapToQuery(filters),
                filtersMapper.mapFacilitiesToQuery(facilities),
                department,
                province,
                district,
                address
            )
            .map { listingsMetadataMapper.mapRemoteToDomain(it) }
    }

    override fun deleteDraftById(id: Long): Completable {
        return listingDataSourceLocal.deleteListingById(id)
    }

    override fun getListings(
        lat: Float?,
        lon: Float?,
        queryText: String?,
        placeId: String?,
        page: Int,
        pageSize: Int,
        radius: Int?,
        sortType: SortType,
        filters: List<Filter>,
        facilities: List<Facility>,
        department: String?,
        province: String?,
        district: String?,
        address: String?
    ): Single<List<Listing>> {
        val listingsSingle = if (page == Constants.FIRST_PAGE) {
            listingDataSourceMemory.deleteAll()
                .andThen(
                    listingDataSourceRemote.getListings(
                        lat,
                        lon,
                        queryText,
                        placeId,
                        page,
                        pageSize,
                        radius,
                        sortType,
                        filtersMapper.mapToQuery(filters),
                        filtersMapper.mapFacilitiesToQuery(facilities),
                        department,
                        province,
                        district,
                        address
                    )
                )
        } else {
            listingDataSourceRemote.getListings(
                lat,
                lon,
                queryText,
                placeId,
                page,
                pageSize,
                radius,
                sortType,
                filtersMapper.mapToQuery(filters),
                filtersMapper.mapFacilitiesToQuery(facilities),
                department,
                province,
                district,
                address
            )
        }

        return listingsSingle
            .flatMap {
                listingDataSourceLocal.saveListings(it.map { listingMapper.mapRemoteToLocal(it) })
                    .andThen(listingDataSourceMemory.saveListings(it.map { listingMapper.mapRemoteToLocal(it) }))
                    .toSingleDefault(it)
            }
            .flatMap { listingDataSourceMemory.getListings(sortType) }
            .map { it.map { listingMapper.mapLocalToDomain(it) } }
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun getFavouriteListings(): Flowable<List<Listing>> {
        val remote = listingDataSourceRemote.getFavouriteListings()
            .map { it.map { listingMapper.mapRemoteToDomain(it) } }
            .flatMap {
                listingDataSourceLocal.saveFavourites(it.map { listingMapper.mapDomainToLocal(it) }).toSingleDefault(it)
            }

        val local = listingDataSourceLocal.getFavouriteListings()
            .map { it.map { listingMapper.mapLocalToDomain(it) } }

        return Single.mergeDelayError(local, remote)
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun setListingAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable {
        val remoteAction = listingDataSourceRemote.setListingAction(
            id,
            key,
            ipAddress,
            userAgent,
            signProvider
        )
        val fullAction = addFavoriteActionIfRequired(remoteAction, id, key, true)
        return fullAction
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun setContactAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable {
        val remoteAction = listingDataSourceRemote.setContactAction(
            id,
            key,
            ipAddress,
            userAgent,
            signProvider
        )
        return remoteAction
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun deleteListingAction(
        id: Long,
        key: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Completable {
        val remoteAction = listingDataSourceRemote.deleteListingAction(
            id,
            key,
            ipAddress,
            userAgent,
            signProvider
        )
        val fullAction = addFavoriteActionIfRequired(remoteAction, id, key, false)
        return fullAction
            .mapNetworkErrors()
            .mapErrors()
    }

    private fun addFavoriteActionIfRequired(
        remoteAction: Completable,
        id: Long,
        key: String,
        isSetAction: Boolean): Completable {
        return if (key == ListingAction.FAVOURITE.name.toLowerCase(Locale.ROOT)) {
            remoteAction
                .mergeWith(listingDataSourceLocal.updateFavorite(id, isSetAction))
                .mergeWith(listingDataSourceMemory.updateFavorite(id, isSetAction))
        } else {
            remoteAction
        }
    }

    override fun updateListing(listingDto: Listing): Single<Listing> {
        return listingDataSourceRemote.updateListing(listingMapper.mapDomainToRemote(listingDto))
            .mapNetworkErrors()
            .mapErrors()
            .flatMap { listingDataSourceLocal.createListing(listingMapper.mapRemoteToLocal(it)) }
            .map { listingMapper.mapLocalToDomain(it) }
    }

    override fun getMyListings(state: String): Flowable<List<Listing>> {
        val draftListings = listingDataSourceLocal.getDraftListings(state=state)
        val remote = listingDataSourceRemote.getMyListings(state=state)
            .flatMap {
                listingDataSourceLocal.saveListings(it.map { listingMapper.mapRemoteToLocal(it) }).toSingleDefault(it)
            }
            .zipWith(draftListings,
                BiFunction<List<ListingJson>, List<ListingDto>, List<Listing>> { remote, draft ->
                    val list = mutableListOf<Listing>()
                    list.addAll(draft.map { listingMapper.mapLocalToDomain(it) })
                    list.addAll(remote.map { listingMapper.mapRemoteToDomain(it) })
                    return@BiFunction list.sortedByDescending { it.id }
                })

        val local = listingDataSourceLocal.getMyListings(state=state)
            .zipWith(draftListings,
                BiFunction<List<ListingDto>, List<ListingDto>, List<Listing>> { local, draft ->
                    val list = mutableListOf<Listing>()
                    list.addAll(draft.map { listingMapper.mapLocalToDomain(it) })
                    list.addAll(local.map { listingMapper.mapLocalToDomain(it) })
                    return@BiFunction list.sortedByDescending { it.id }
                })

        return Single.mergeDelayError(local, remote)
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun getDraftListingById(id: Long): Single<Listing> {
        val userSingle = authDataSourceLocal.getUser()
        return listingDataSourceLocal.getListingById(id)
            .zipWith(userSingle, BiFunction<ListingDto, UserDto, Listing> { listing, user ->
                listing.user = user
                listingMapper.mapLocalToDomain(listing)
            })
    }

    override fun createListing(listingDto: Listing): Single<Listing> {
        return listingDataSourceRemote.createListing(listingMapper.mapDomainToRemote(listingDto))
            .mapNetworkErrors()
            .mapErrors()
            .flatMap { listingDataSourceLocal.createListing(listingMapper.mapRemoteToLocal(it)) }
            .flatMap { listingDto.id?.let { id -> listingDataSourceLocal.deleteListingById(id).toSingleDefault(it) } }
            .map { listingMapper.mapLocalToDomain(it) }

    }

    override fun getFacilitiesByType(dataType: FacilityDataType, propertyType: String): Flowable<List<Facility>> {
        val remote = listingDataSourceRemote.getFacilitiesByType(dataType.strType, propertyType)
            .map { it.map { facilityMapper.mapRemoteToDomain(it) } }
            .flatMap {
                if (dataType == FacilityDataType.FACILITY) {
                    listingDataSourceLocal.saveFacilities(
                        propertyType,
                        it.map { facilityMapper.mapDomainToLocal(it) })
                        .toSingleDefault(it)
                } else {
                    listingDataSourceLocal.saveAdvancedDetails(
                        propertyType,
                        it.map { facilityMapper.mapDomainToLocal(it) })
                        .toSingleDefault(it)
                }
            }
        val local = if (dataType == FacilityDataType.FACILITY) {
            listingDataSourceLocal.getFacilitiesByType(propertyType)
        } else {
            listingDataSourceLocal.getAdvancedDetailsByType(propertyType)
        }.map { it.map { facilityMapper.mapLocalToDomain(it) } }
        return Single.mergeDelayError(local, remote)
            .mapNetworkErrors()
    }

    override fun uploadListingImage(path: String): Single<List<ListingImage>> {
        return listingDataSourceRemote.uploadListingImage(path)
            .map { it.map { listingImageMapper.mapRemoteToDomain(it) } }
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun draftListing(listingDto: Listing): Single<Listing> {
        return listingDataSourceLocal.createListing(listingMapper.mapDomainToLocal(listingDto))
            .map { listingMapper.mapLocalToDomain(it) }
    }

    override fun getListingById(id: Long): Single<Listing> {
        return listingDataSourceRemote.getListingById(id)
            .flatMap { listingDataSourceLocal.updateListing(listingMapper.mapRemoteToLocal(it)) }
            .flatMap { listingDataSourceMemory.updateListing(it) }
            .map { listingMapper.mapLocalToDomain(it) }
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun getMyListingById(id: Long): Single<Listing> {
        return listingDataSourceRemote.getMyListingById(id)
            .flatMap { listingDataSourceLocal.updateListing(listingMapper.mapRemoteToLocal(it)) }
            .flatMap { listingDataSourceMemory.updateListing(it) }
            .map { listingMapper.mapLocalToDomain(it) }
            .mapNetworkErrors()
            .mapErrors()
    }

    override fun getLocalListing(id: Long): Single<Listing> {
        return listingDataSourceLocal.getListingById(id)
            .map { listingMapper.mapLocalToDomain(it) }
    }

    override fun getRecentSearch(): Single<List<RecentSearch>> {
        return listingDataSourceRemote.getRecentSearches()
            .mapNetworkErrors()
            .mapErrors()
            .map { recentSearchMapper.mapRemoteToDomain(it) }
    }

    override fun getTopListings(lat: Float, lon: Float, radius: Int): Single<List<Listing>> {
        return listingDataSourceRemote.getTopListings(lat, lon, radius)
            .mapNetworkErrors()
            .mapErrors()
            .map { listings ->
                listings.map { listingMapper.mapRemoteToDomain(it) }
            }
    }

    override fun getDataLocationSearched(address: String, page: Int, perPage: Int): Single<DataLocation> {
        return listingDataSourceRemote.getDataLocationSearched(address, page, perPage)
            .map { dataLocationMapper.mapRemoteToDomain(it) }
    }

    override fun getListUbigeo(type: String, department: String?, province: String?): Single<List<String>> {
        return listingDataSourceRemote.getListUbigeo(type, department, province)
            .mapNetworkErrors()
            .mapErrors()
    }
}