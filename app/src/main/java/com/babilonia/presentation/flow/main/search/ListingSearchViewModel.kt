package com.babilonia.presentation.flow.main.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.Constants.LIMA_LAT
import com.babilonia.Constants.LIMA_LON
import com.babilonia.R
import com.babilonia.android.exceptions.GpsUnavailableException
import com.babilonia.android.system.AppSystemProvider
import com.babilonia.data.model.DataResult
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.domain.model.*
import com.babilonia.domain.model.enums.FacilityDataType
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.model.enums.SortType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.*
import com.babilonia.domain.usecase.ar.NeedToShowArOnboardingUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.common.ListingActionsListener
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.createlisting.ID
import com.babilonia.presentation.flow.main.publish.createlisting.MODE
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilityChangeListener
import com.babilonia.presentation.flow.main.search.common.FiltersDelegate
import com.babilonia.presentation.flow.main.search.common.FiltersDelegateImpl
import com.babilonia.presentation.flow.main.search.model.FiltersVisibility
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import com.babilonia.presentation.utils.SvgUtil.updateCoordinateLocation
import com.babilonia.presentation.utils.SvgUtil.updateILocation
import com.babilonia.presentation.utils.SvgUtil.updatePlaceLocation
import com.babilonia.presentation.view.priceview.BarEntry
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.DisposableSubscriber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ListingSearchViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    private val listingActionUseCase: ListingActionUseCase,
    private val getLocationUseCase: GetLastKnownLocationUseCase,
    private val getCurrentPlaceUseCase: GetCurrentPlaceUseCase,
    private val getListingsMetadataUseCase: GetListingsMetadataUseCase,
    private val getPriceRangeUseCase: GetPriceRangeUseCase,
    private val getFacilitiesUseCase: GetFacilitiesUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getTopListingsUseCase: GetTopListingsUseCase,
    private val appSystemProvider: AppSystemProvider,
    private val needToShowArOnboardingUseCase: NeedToShowArOnboardingUseCase,
    private val getDataLocationSearchedUseCase: GetDataLocationSearchedUseCase,
    private val getDataListingsPageUseCase: GetListingsPageUseCase
) : BaseViewModel(), ListingActionsListener, FiltersDelegate by FiltersDelegateImpl(),
    FacilityChangeListener {
    val authFailedData = SingleLiveEvent<Unit>()
    val topListingsLiveData = MutableLiveData<List<Listing>>()
    val listings = MutableLiveData<List<Listing>>()
    val locationLiveData = MutableLiveData<ILocation>()
    val priceRangeLiveData = MutableLiveData<List<BarEntry>>()
    val suggestions = MutableLiveData<List<Location>>()
    val currentPage = MutableLiveData<DataCurrentPage>()
    val recentSearches = MutableLiveData<List<RecentSearch>>()
    val onFocusChangeEvent = MutableLiveData<Boolean>()
    val filtersVisibilityLiveData = MutableLiveData<FiltersVisibility>(FiltersVisibility.All())
    val topListingsVisibilityLiveData = MutableLiveData<Boolean>(true)
    val clearSearchFromMapLiveData = MutableLiveData<Boolean>(false)
    val notClearSearchLiveData = MutableLiveData<Boolean>(false)

    val onPlaceFoundEvent = SingleLiveEvent<PlaceLocation>()
    val currentPlace = SingleLiveEvent<PlaceLocation>()
    val gpsUnavailableError = SingleLiveEvent<Unit>()
    val gotFacilitiesEvent = SingleLiveEvent<List<Facility>>()
    val arOnboardingLiveData = SingleLiveEvent<Unit>()

    val listingsMetadata = MutableLiveData<ListingsMetadata>()
    val userIdLiveData = MutableLiveData<Long>()

    var metadataUpdateSubject = BehaviorSubject.create<Boolean>()
    val paginator = PublishProcessor.create<Int>()
    var lastLoadedPageIndex: Int = 1
        private set
    var needToResetPaginator = false
    var needToResetAdapter = false
    var topListingsSavedState: Parcelable? = null

    var searchingByCurrentPlace = true
    var sortingBy = SortType.MOST_RELEVANT.ordinal
    var currentZoom = 6f
    var searchRadius = Constants.DEFAULT_RADIUS

    val isGPSLocationLiveData = MutableLiveData<Boolean>(false)
    val isShowScreenMapLiveData = MutableLiveData<Boolean>(false)
    val isSearchWithSwipeLiveData = MutableLiveData<Boolean>(false)
    val isSearchByBoundsLiveData = MutableLiveData<Boolean>(false)
    val isSearchByAutocompleteLiveData = MutableLiveData<Boolean>(false)
    val isForcedLocationGPSLiveData = MutableLiveData<Boolean>(false)

    val placeLocationGPSLiveData = SingleLiveEvent<PlaceLocation>()

    var ipAddress = ""
    var total_pages = 0
    var pagesInfo = 1
    var per_pages = 0

    init {
        subscribeToListingMetadata()
    }

    override fun onFavouriteClicked(isChecked: Boolean, id: Long) {
        val mode = if (isChecked) {
            ListingActionMode.SET
        } else {
            ListingActionMode.DELETE
        }

        listingActionUseCase.execute(
            object : DisposableCompletableObserver() {
                override fun onComplete() {
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else
                        dataError.postValue(e)
                }

            }, ListingActionUseCase.Params(
                id,
                ListingAction.FAVOURITE,
                mode,
                ipAddress,
                "android",
                "email"
            )
        )
    }

    override fun onPreviewClicked(listing: Listing) {
        val mode = if (listing.user?.id == userIdLiveData.value) {
            if (listing.status == Constants.HIDDEN)
                ListingDisplayMode.UNPUBLISHED
            else
                ListingDisplayMode.PUBLISHED
        } else {
            ListingDisplayMode.IMPROPER_LISTING
        }

        listing.id?.let { id ->
            navigateGlobal(
                R.id.action_global_listingFragment,
                bundleOf(
                    ID to id,
                    MODE to mode
                )
            )
        }
    }

    override fun onChange(value: Facility?) {
        value?.let {
            if (it.isChecked) {
                addFacility(it)
            } else {
                removeFacility(it)
            }
            metadataUpdateSubject.onNext(true)
        }
    }

    fun onAllFacilitiesClicked(isChecked: Boolean) {
        if (isChecked) {
            gotFacilitiesEvent.value?.forEach { addFacility(it) }
        } else {
            clearTempFacilities()
        }
        metadataUpdateSubject.onNext(true)
    }

    fun applyFiltersAndBack(context: Context) {
        setSearchByBounds(true)
        setIsForcedLocationGPS(true)
        needToResetPaginator = true
        applyFilters(context.resources)
        navigateBack()
    }

    fun getListingsWithLocationPermission(isPermissionGranted: Boolean) {
        startLoading()
        if (locationLiveData.value == null) {
            if (isPermissionGranted) {
                getLocation()
            } else {
                getDefaultPlace()
            }
        } else {
            stopLoading()
        }
    }

    fun getLocation(onLocationFound: ((location: ILocation) -> Unit)? = null) {
        searchingByCurrentPlace = true
        setIsGPSLocation(true)
        getLocationUseCase.execute(object : DisposableSingleObserver<DataResult<ILocation>>() {
            override fun onSuccess(result: DataResult<ILocation>) {
                when (result) {
                    is DataResult.Success -> {
                        stopLoading()
                        val mLocation = updateILocation(result.data)
                        onLocationFound?.invoke(mLocation)
                        locationLiveData.postValue(mLocation)
                        onPlaceFoundEvent.postValue(
                            PlaceLocation(
                                mLocation.latitude,
                                mLocation.longitude,
                                mLocation.altitude,
                                mLocation.address,
                                LatLngBounds(
                                    LatLng(mLocation.latitude, mLocation.longitude),
                                    LatLng(mLocation.latitude, mLocation.longitude)
                                ),
                                null,
                                mLocation.department,
                                mLocation.province,
                                mLocation.district,
                                mLocation.zipCode,
                                mLocation.country
                            )
                        )
                        metadataUpdateSubject.onNext(false)
                    }
                    is DataResult.Error -> {
                        getDefaultPlace()
                        if (result.throwable is GpsUnavailableException) {
                            gpsUnavailableError.call()
                        } else {
                            //  dataError.postValue(result.throwable)
                        }
                    }
                }
            }

            override fun onError(e: Throwable) {
                stopLoading()
                dataError.postValue(e)
            }

        }, Unit)
    }

    fun getDefaultPlace() {
        authStorageLocal.setValidateDefaultLocation(true)
        setIsGPSLocation(true)
        val mLocation = updatePlaceLocation(
            PlaceLocation(
                LIMA_LAT,
                LIMA_LON,
                0.0,
                null,
                LatLngBounds(LatLng(LIMA_LAT, LIMA_LON), LatLng(LIMA_LAT, LIMA_LON)),
                null,
                null,
                null,
                null,
                null,
                null
            )
        )
        stopLoading()
        locationLiveData.postValue(mLocation)
        onPlaceFoundEvent.postValue(mLocation)
        metadataUpdateSubject.onNext(false)
    }

    fun getFacilities(type: String) {
        getFacilitiesUseCase.execute(object : DisposableSubscriber<List<Facility>>() {
            override fun onComplete() {

            }

            override fun onNext(facilities: List<Facility>) {
                gotFacilitiesEvent.value = facilities
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.value = e
            }

        }, GetFacilitiesUseCase.Params(FacilityDataType.FACILITY, type.toLowerCase(Locale.ROOT)))
    }

    fun onPropertyTypeChanged(newType: String) {
        clearTempFacilities()
        filtersVisibilityLiveData.value = FiltersVisibility.getVisibilityByPropertyName(newType)
    }

    fun initTempFacilities() {
        clearTempFacilities()
        getFacilities().forEach { addFacility(it) }
    }

    fun getPlaces(address: String, page: Int, perPage: Int) {
        getDataLocationSearchedUseCase.execute(
            object : DisposableSingleObserver<DataLocation>() {
                override fun onSuccess(dataLocation: DataLocation) {
                    suggestions.postValue(dataLocation.location ?: emptyList())
                }

                override fun onError(e: Throwable) {
                    //dataError.postValue(e)
                }

            }, GetDataLocationSearchedUseCase.Params(
                address = address,
                page = page,
                perPage = perPage
            )
        )
    }

    fun getListingsPage(location: ILocation, pageSize: Int = Constants.PER_PAGE) {
        getDataListingsPageUseCase.execute(
            object : DisposableSingleObserver<DataCurrentPage>() {
                override fun onSuccess(dataCurrentPage: DataCurrentPage) {
                    currentPage.postValue(dataCurrentPage)
                    total_pages = dataCurrentPage.pagination!!.totalPages
                    per_pages = dataCurrentPage.pagination.perPage
                }

                override fun onError(e: Throwable) {
                    //dataError.postValue(e)
                }

            }, GetListingsPageUseCase.Params(
                page = pageSize,
                sortType = SortType.getByPosition(sortingBy),
                department = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.department,
                province = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.province,
                district = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.district,
                address = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.address
            )
        )
    }

    fun getPriceRange(shouldUseTmpFilters: Boolean = true) {
        if (shouldUseTmpFilters) {
            getPriceRange(getTempFilters(), getTempFacilities())
        } else {
            getPriceRange(getFilters(), getFacilities())
        }
    }

    private fun getPriceRange(filters: List<Filter>, facilities: List<Facility>) {

        if (filters.isEmpty()) return

        locationLiveData.value?.let {
            getPriceRangeUseCase.execute(
                object : DisposableSingleObserver<List<BarEntry>>() {
                    override fun onSuccess(priceRange: List<BarEntry>) {
                        priceRangeLiveData.postValue(priceRange)
                    }

                    override fun onError(e: Throwable) {
                        if (e is AuthFailedException) {
                            signOut {
                                authFailedData.call()
                            }
                        } else
                            dataError.postValue(e)
                    }
                },
                GetPriceRangeUseCase.Params(
                    it.latitude.toFloat(),
                    it.longitude.toFloat(),
                    searchRadius,
                    filters,
                    facilities
                )
            )
        }
    }

    fun onFiltersClicked() {
        navigate(R.id.action_searchRootFragment_to_listingFiltersFragment)
    }

    fun setLocationFromMapPosition(lat: Double, lon: Double): ILocation {
        setSearchWithSwipeMap(true)
        val location = Location().apply {
            latitude = lat
            longitude = lon
        }
        locationLiveData.postValue(location)
        return updateILocation(location)
    }

    fun getRecentSearches() {

    }

    fun getUserId() {
        getUserIdUseCase.execute(object : DisposableSingleObserver<Long>() {
            override fun onSuccess(userId: Long) {
                userIdLiveData.value = userId
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.postValue(e)
            }
        }, Unit)
    }

    fun navigateToAr() {
        navigateGlobal(R.id.action_global_ar)
    }

    fun isLocationEnabled() = appSystemProvider.isLocationEnabled()

    fun arButtonClicked() {
        needToShowArOnboardingUseCase.execute(object : DisposableSingleObserver<Boolean>() {
            override fun onSuccess(needToShowOnboarding: Boolean) {
                if (needToShowOnboarding) {
                    arOnboardingLiveData.call()
                } else {
                    navigateToAr()
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    private fun getListingsMetadata(filters: List<Filter>, facilities: List<Facility>) {
        locationLiveData.value?.let {
            getListingsMetadataUseCase.execute(
                object : DisposableObserver<ListingsMetadata>() {
                    override fun onComplete() {

                    }

                    override fun onNext(metadata: ListingsMetadata) {
                        listingsMetadata.postValue(metadata)
                    }

                    override fun onError(e: Throwable) {
                        dataError.postValue(e)
                    }
                },
                GetListingsMetadataUseCase.Params(
                    if (getIsGPSLocation() || getIsShowScreenMap()) it.latitude.toFloat() else null,
                    if (getIsGPSLocation() || getIsShowScreenMap()) it.longitude.toFloat() else null,
                    if (getIsGPSLocation() || getIsShowScreenMap()) Constants.DEFAULT_RADIUS else null,
                    filters,
                    facilities,
                    if (getIsGPSLocation() || getIsShowScreenMap()) null else it.department,
                    if (getIsGPSLocation() || getIsShowScreenMap()) null else it.province,
                    if (getIsGPSLocation() || getIsShowScreenMap()) null else it.district,
                    if (getIsGPSLocation() || getIsShowScreenMap()) null else it.address
                )
            )
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToListingMetadata() {
        metadataUpdateSubject.debounce(Constants.DEFAULT_DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe { shouldUseTmpFilters ->
                val filters: List<Filter>
                val facilities: List<Facility>
                if (shouldUseTmpFilters) {
                    filters = getTempFilters()
                    facilities = getTempFacilities()
                } else {
                    filters = getFilters()
                    facilities = getFacilities()
                }
                getListingsMetadata(filters, facilities)
                //getPriceRange(filters, facilities)
            }
    }

    fun getTopListings(location: ILocation, radius: Int = Constants.MAX_RADIUS) {
        if (isTopListingVisible().not()) {
            return
        } else {
            Log.i("RCS-ENTER ", "getTopListings")
        }

        getTopListingsUseCase.execute(
            object : DisposableSingleObserver<List<Listing>>() {
                override fun onSuccess(topListings: List<Listing>) {
                    topListingsLiveData.value = topListings
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        dataError.postValue(e)
                        e.printStackTrace()
                    }
                }
            }, GetTopListingsUseCase.Params(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                radius
            )
        )
    }

    fun getTopListingsLoading(location: ILocation, radius: Int = Constants.MAX_RADIUS) {
        startLoading()
        if (isTopListingVisible().not()) {
            //   listings.postValue(listOf())
            stopLoading()
            return
        } else {
            Log.i("RCS-ENTER ", "getTopListingsLoading")
        }

        getTopListingsUseCase.execute(
            object : DisposableSingleObserver<List<Listing>>() {
                override fun onSuccess(topListings: List<Listing>) {
                    stopLoading()
                    //  listings.postValue(listOf())
                    topListingsLiveData.value = topListings
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        stopLoading()
                        //     listings.postValue(listOf())
                        dataError.postValue(e)
                        e.printStackTrace()
                    }
                }
            }, GetTopListingsUseCase.Params(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                radius
            )
        )
    }

    private fun updateTopListingsVisibility() {
        topListingsVisibilityLiveData.value = isTopListingVisible()
    }

    private fun isTopListingVisible(): Boolean {
        val visibleByFilters = hasFilters().not()
        return visibleByFilters && searchingByCurrentPlace
    }

    fun getListings() {
        val chosenLocation = locationLiveData.value
        if (chosenLocation != null) {
            getListings(chosenLocation)
        } else {
            getLocation()
        }
    }

    fun getListings(location: ILocation, pageSize: Int = Constants.PER_PAGE) {
        startLoading()
        disposables.clear()

        val disposable = paginator.onBackpressureDrop().subscribe { pageIndex ->
            getListingsUseCase.execute(
                object : DisposableSingleObserver<List<Listing>>() {
                    override fun onSuccess(list: List<Listing>) {

                        if (!authStorageLocal.isValidateDefaultLocation() && list.isNullOrEmpty() && pageIndex == 1) {
                            authStorageLocal.setValidateDefaultLocation(true)
                            getDefaultPlace()
                        } else {
                            stopLoading()
                            authStorageLocal.setValidateDefaultLocation(true)
                            restartMetadata()
                            lastLoadedPageIndex = pageIndex
                            updateTopListingsVisibility()
                            getRecentSearches()
                            listings.postValue(list)
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (e is AuthFailedException) {
                            signOut {
                                authFailedData.call()
                            }
                        } else {
                            stopLoading()
                            authStorageLocal.setValidateDefaultLocation(true)
                            if (pageIndex <= 1) {
                                listings.postValue(emptyList())
                                updateTopListingsVisibility()
                                getRecentSearches()
                            }
                            dataError.postValue(e)
                        }
                    }

                }, GetListingsUseCase.Params(
                    lat = if (getIsGPSLocation() || getIsShowScreenMap()) location.latitude.toFloat() else null,
                    lon = if (getIsGPSLocation() || getIsShowScreenMap()) location.longitude.toFloat() else null,
                    queryText = if (location.address.isNullOrEmpty()) null else location.address,
                    placeId = if (location is PlaceLocation) location.googlePlaceId else null,
                    page = pageIndex,
                    radius = if (getIsGPSLocation() || getIsShowScreenMap()) Constants.DEFAULT_RADIUS else null,
                    sortType = SortType.getByPosition(sortingBy),
                    filters = getFilters(),
                    facilities = getFacilities(),
                    pageSize = pageSize,
                    department = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.department,
                    province = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.province,
                    district = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.district,
                    address = if (getIsGPSLocation() || getIsShowScreenMap()) null else location.address
                )
            )
        }

        val pageIndex = if (needToResetPaginator) {
            needToResetPaginator = false
            needToResetAdapter = true

            Constants.FIRST_PAGE
        } else {
            lastLoadedPageIndex
        }

        paginator.onNext(pageIndex)
        disposables.add(disposable)
    }

    fun getListingsFromMapWithCoordinate(location: ILocation, pageSize: Int = Constants.PER_PAGE) {
        startLoading()
        disposables.clear()
        val disposable = paginator.onBackpressureDrop().subscribe { pageIndex ->
            getListingsUseCase.execute(
                object : DisposableSingleObserver<List<Listing>>() {
                    override fun onSuccess(list: List<Listing>) {
                        stopLoading()
                        restartMetadata()
                        setSearchWithSwipeMap(true)
                        setIsGPSLocation(true)
                        setSearchByBounds(false)
                        lastLoadedPageIndex = pageIndex
                        updateTopListingsVisibility()
                        listings.postValue(list)
                        getRecentSearches()
                    }

                    override fun onError(e: Throwable) {
                        if (e is AuthFailedException) {
                            signOut {
                                authFailedData.call()
                            }
                        } else {
                            stopLoading()
                            setSearchWithSwipeMap(false)
                            if (pageIndex <= 1) {
                                listings.postValue(emptyList())
                                updateTopListingsVisibility()
                                getRecentSearches()
                            }
                            //dataError.postValue(e)
                        }
                    }

                }, GetListingsUseCase.Params(
                    lat = if (getIsGPSLocation() || getSearchWithSwipeMap()) location.latitude.toFloat() else null,
                    lon = if (getIsGPSLocation() || getSearchWithSwipeMap()) location.longitude.toFloat() else null,
                    queryText = if (location.address.isNullOrEmpty()) null else location.address,
                    placeId = if (location is PlaceLocation) location.googlePlaceId else null,
                    page = pageIndex,
                    radius = if (getIsGPSLocation() || getSearchWithSwipeMap()) Constants.DEFAULT_RADIUS else null,
                    sortType = SortType.getByPosition(sortingBy),
                    filters = getFilters(),
                    facilities = getFacilities(),
                    pageSize = pageSize,
                    department = if (getIsGPSLocation() || getSearchWithSwipeMap()) null else location.department,
                    province = if (getIsGPSLocation() || getSearchWithSwipeMap()) null else location.province,
                    district = if (getIsGPSLocation() || getSearchWithSwipeMap()) null else location.district,
                    address = if (getIsGPSLocation() || getSearchWithSwipeMap()) null else location.address
                )
            )
        }

        val pageIndex = if (needToResetPaginator) {
            needToResetPaginator = false
            needToResetAdapter = true

            Constants.FIRST_PAGE
        } else {
            lastLoadedPageIndex
        }

        paginator.onNext(pageIndex)
        disposables.add(disposable)
    }

    fun getListingsFromMapNoCoordinate(location: ILocation, pageSize: Int = Constants.PER_PAGE) {
        startLoading()
        disposables.clear()
        val disposable = paginator.onBackpressureDrop().subscribe { pageIndex ->
            getListingsUseCase.execute(
                object : DisposableSingleObserver<List<Listing>>() {
                    override fun onSuccess(list: List<Listing>) {
                        stopLoading()
                        restartMetadata()
                        setSearchWithSwipeMap(false)
                        setIsGPSLocation(false)
                        setSearchByBounds(true)
                        lastLoadedPageIndex = pageIndex
                        updateTopListingsVisibility()
                        listings.postValue(list)
                        getRecentSearches()
                    }

                    override fun onError(e: Throwable) {
                        if (e is AuthFailedException) {
                            signOut {
                                authFailedData.call()
                            }
                        } else {
                            stopLoading()
                            setSearchWithSwipeMap(false)
                            if (pageIndex <= 1) {
                                listings.postValue(emptyList())
                                updateTopListingsVisibility()
                                getRecentSearches()
                            }
                            //dataError.postValue(e)
                        }
                    }

                }, GetListingsUseCase.Params(
                    lat = if (getIsGPSLocation()) location.latitude.toFloat() else null,
                    lon = if (getIsGPSLocation()) location.longitude.toFloat() else null,
                    queryText = if (location.address.isNullOrEmpty()) null else location.address,
                    placeId = if (location is PlaceLocation) location.googlePlaceId else null,
                    page = pageIndex,
                    radius = if (getIsGPSLocation()) Constants.DEFAULT_RADIUS else null,
                    sortType = SortType.getByPosition(sortingBy),
                    filters = getFilters(),
                    facilities = getFacilities(),
                    pageSize = pageSize,
                    department = if (getIsGPSLocation()) null else location.department,
                    province = if (getIsGPSLocation()) null else location.province,
                    district = if (getIsGPSLocation()) null else location.district,
                    address = if (getIsGPSLocation()) null else location.address
                )
            )
        }

        val pageIndex = if (needToResetPaginator) {
            needToResetPaginator = false
            needToResetAdapter = true

            Constants.FIRST_PAGE
        } else {
            lastLoadedPageIndex
        }

        paginator.onNext(pageIndex)
        disposables.add(disposable)
    }

    fun getCurrentPlace() {
        searchingByCurrentPlace = true
        startLoading()
        getCurrentPlaceUseCase.execute(object : DisposableSingleObserver<PlaceLocation>() {
            override fun onSuccess(location: PlaceLocation) {
                location.googlePlaceId = null
                searchRadius = Constants.DEFAULT_RADIUS
                setIsGPSLocation(true)
                val placeLocation = updatePlaceLocation(location)
                searchingByCurrentPlace = true
                currentPlace.postValue(placeLocation)
                locationLiveData.postValue(placeLocation)
                onPlaceFoundEvent.postValue(placeLocation)
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else {
                    dataError.postValue(e)
                    stopLoading()
                }
            }
        }, Unit)
    }

    fun getLocationSelected(location: Location, mSearchingByCurrentPlace: Boolean) {
        searchingByCurrentPlace = mSearchingByCurrentPlace
        setIsGPSLocation(false)
        setIsSearchByAutocomplete(true)
        val mLocation = updateCoordinateLocation(location, listings.value)
        locationLiveData.postValue(mLocation)
        onPlaceFoundEvent.postValue(
            PlaceLocation(
                mLocation.latitude,
                mLocation.longitude,
                mLocation.altitude,
                mLocation.address,
                mLocation.viewport,
                null,
                mLocation.department,
                mLocation.province,
                mLocation.district,
                mLocation.zipCode,
                mLocation.country
            )
        )
    }

    fun clearSearchFromMap(isCleared: Boolean) {
        clearSearchFromMapLiveData.postValue(isCleared)
    }

    fun notClearSearchFromMap() {
        notClearSearchLiveData.postValue(true)
    }

    fun setIsGPSLocation(status: Boolean) {
        isGPSLocationLiveData.value = status
    }

    fun getIsGPSLocation(): Boolean {
        return isGPSLocationLiveData.value ?: false
    }

    fun setIsSearchByAutocomplete(status: Boolean) {
        isSearchByAutocompleteLiveData.value = status
    }

    fun getIsSearchByAutocomplete(): Boolean {
        return isSearchByAutocompleteLiveData.value ?: false
    }

    fun setIsShowScreenMap(status: Boolean) {
        isShowScreenMapLiveData.value = status
    }

    fun getIsShowScreenMap(): Boolean {
        return isShowScreenMapLiveData.value ?: false
    }

    fun setSearchWithSwipeMap(status: Boolean) {
        isSearchWithSwipeLiveData.value = status
    }

    fun getSearchWithSwipeMap(): Boolean {
        return isSearchWithSwipeLiveData.value ?: false
    }

    fun restartMetadata() {
        metadataUpdateSubject.onNext(true)
    }

    fun setSearchByBounds(status: Boolean) {
        isSearchByBoundsLiveData.value = status
    }

    fun getSearchByBounds(): Boolean {
        return isSearchByBoundsLiveData.value ?: false
    }

    fun setIsForcedLocationGPS(status: Boolean) {
        isForcedLocationGPSLiveData.value = status
    }

    fun getIsForcedLocationGPS(): Boolean {
        return isForcedLocationGPSLiveData.value ?: false
    }
}

