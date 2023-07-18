package com.babilonia.data.storage.listing

import com.babilonia.BuildConfig
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.ListingsDataSourceLocal
import com.babilonia.data.db.DataBaseConstants
import com.babilonia.data.db.model.*
import com.babilonia.domain.model.enums.SortType
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject

// Created by Anton Yatsenko on 07.06.2019.
class ListingStorageLocal @Inject constructor(private val config: RealmConfiguration) : ListingsDataSourceLocal {
    override fun deleteAll(): Completable {
        return Completable.fromAction {
            Realm.getInstance(config)
                .use {
                    it.executeTransaction {
                        it.where(ListingDto::class.java).findAll().deleteAllFromRealm()
                    }
                }
        }

    }

    override fun getListings(sortType: SortType): Single<List<ListingDto>> {
        return Single.create<List<ListingDto>> { single ->
            val instance = Realm.getInstance(config)
            val listingDto = instance.where(ListingDto::class.java).findAll()
            if (listingDto != null) {
                single.onSuccess(instance.copyFromRealm(listingDto))
            } else {
                single.onSuccess(emptyList())
            }
        }
    }

    override fun getFavouriteListings(): Single<List<ListingDto>> {
        return Single.create<List<ListingDto>> { single ->
            Realm.getInstance(config)
                .use {
                    val listingDto = it.where(ListingDto::class.java)
                        .equalTo("favourited", true).equalTo("status", Constants.VISIBLE).findAll()
                    if (listingDto != null) {
                        single.onSuccess(it.copyFromRealm(listingDto))
                    }
                }
        }
    }

    override fun saveFavourites(listings: List<ListingDto>): Completable {
        return Completable.create {
            Realm.getInstance(config)
                .use { realm ->
                    realm.executeTransaction {
                        it.where(ListingDto::class.java)
                            .equalTo("favourited", true).findAll().deleteAllFromRealm()
                        it.copyToRealmOrUpdate(listings)
                    }
                    it.onComplete()
                }
        }

    }

    override fun saveListings(listings: List<ListingDto>): Completable {
        return Completable.create {
            Realm.getInstance(config)
                .use { realm ->
                    realm.executeTransaction {
                        it.copyToRealmOrUpdate(listings)
                    }
                    it.onComplete()
                }
        }
    }

    override fun getMyListings(): Single<List<ListingDto>> {
        return Single.create<List<ListingDto>> { single ->
            Realm.getInstance(config)
                .use {
                    val userId = it.where(UserDto::class.java).findFirst()?.id ?: EmptyConstants.EMPTY_LONG
                    val listingDto = it.where(ListingDto::class.java).equalTo("user.id", userId).findAll()
                    if (listingDto != null) {
                        single.onSuccess(it.copyFromRealm(listingDto))
                    }
                }
        }
    }

    override fun getDraftListings(): Single<List<ListingDto>> {
        return Single.create<List<ListingDto>> { single ->
            Realm.getInstance(config)
                .use {
                    val listingDto = it.where(ListingDto::class.java).equalTo("draft", true).findAll()
                    if (listingDto != null) {
                        single.onSuccess(it.copyFromRealm(listingDto))
                    } else {
                        single.onSuccess(emptyList())
                    }
                }
        }
    }

    override fun updateFavorite(id: Long, isFavourite: Boolean): Completable {
        return Completable.create { emitter ->
            Realm.getInstance(config).use { realm ->
                realm.executeTransaction { realm ->
                    val dto = realm.where(ListingDto::class.java).equalTo(DataBaseConstants.ID, id).findFirst()
                    dto?.favourited = isFavourite
                    dto?.let {
                        realm.copyToRealmOrUpdate(it)
                    }
                    if (BuildConfig.DEBUG) {
                        Timber.tag(javaClass.simpleName).d("Deleted from realm")
                    }
                    emitter.onComplete()
                }

            }
        }
    }

    override fun deleteListingById(id: Long): Completable {
        return Completable.create { emitter ->
            Realm.getInstance(config).use { realm ->
                realm.executeTransaction {
                    it.where(ListingDto::class.java).equalTo(DataBaseConstants.ID, id).findFirst()?.deleteFromRealm()
                    if (BuildConfig.DEBUG) {
                        Timber.tag(javaClass.simpleName).d("Deleted from realm")
                    }
                    emitter.onComplete()
                }

            }
        }
    }

    override fun getListingById(id: Long): Single<ListingDto> {
        return Single.create<ListingDto> { single ->
            Realm.getInstance(config)
                .use {
                    val listingDto = it.where(ListingDto::class.java).equalTo(DataBaseConstants.ID, id).findFirst()
                    if (listingDto != null) {
                        single.onSuccess(it.copyFromRealm(listingDto))
                    }
                }
        }
    }

    override fun uploadListingImage(path: String): Single<ImageDto> {
        throw NotImplementedError()
    }

    override fun createListing(listingDto: ListingDto): Single<ListingDto> {
        return Single.create<ListingDto> { single ->
            Realm.getInstance(config)
                .use {
                    it.executeTransaction { realm ->
                        realm.copyToRealmOrUpdate(listingDto)
                    }
                    single.onSuccess(listingDto)
                }
        }
    }

    override fun saveFacilities(type: String, data: List<FacilityDto>): Completable {
        return Completable.create {
            val dto = FacilitiesDto()
            dto.data.addAll(data)
            dto.id = type
            Realm.getInstance(config)
                .use {
                    it.executeTransaction { realm ->
                        realm.copyToRealmOrUpdate(dto)
                    }
                }
            it.onComplete()
        }

    }

    override fun getFacilitiesByType(type: String): Single<List<FacilityDto>> {
        val first =
            Realm.getInstance(config).where(FacilitiesDto::class.java).equalTo(DataBaseConstants.ID, type).findFirst()
        return if (first != null) {
            Single.just(Realm.getInstance(config).copyFromRealm(first).data)
        } else {
            Single.just(emptyList())
        }
    }

    override fun saveAdvancedDetails(type: String, data: List<FacilityDto>): Completable {
        return Completable.create {
            val dto = AdvancedDetailsDto()
            dto.data.addAll(data)
            dto.id = type
            Realm.getInstance(config)
                .use {
                    it.executeTransaction { realm ->
                        realm.copyToRealmOrUpdate(dto)
                    }
                }
            it.onComplete()
        }

    }

    override fun getAdvancedDetailsByType(type: String): Single<List<FacilityDto>> {
        val first =
            Realm.getInstance(config).where(AdvancedDetailsDto::class.java).equalTo(DataBaseConstants.ID, type).findFirst()
        return if (first != null) {
            Single.just(Realm.getInstance(config).copyFromRealm(first).data)
        } else {
            Single.just(emptyList())
        }
    }

    override fun updateListing(listingDto: ListingDto): Single<ListingDto> {
        return Single.create { emitter ->
            Realm.getInstance(config).use { realm ->
                realm.executeTransaction { realm ->
                    realm.insertOrUpdate(listingDto)
                    emitter.onSuccess(listingDto)
                }
            }
        }
    }
}