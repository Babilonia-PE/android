package com.babilonia.presentation.flow.main.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.android.exceptions.GpsUnavailableException
import com.babilonia.android.system.AppSystemProvider
import com.babilonia.data.model.DataResult
import com.babilonia.data.network.error.NoNetworkException
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
import com.babilonia.presentation.extension.safeLet
import com.babilonia.presentation.flow.main.common.ListingActionsListener
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.createlisting.ID
import com.babilonia.presentation.flow.main.publish.createlisting.MODE
import com.babilonia.presentation.flow.main.publish.facilities.common.FacilityChangeListener
import com.babilonia.presentation.flow.main.search.common.FiltersDelegate
import com.babilonia.presentation.flow.main.search.common.FiltersDelegateImpl
import com.babilonia.presentation.flow.main.search.map.AndroidLocation
import com.babilonia.presentation.flow.main.search.model.FiltersVisibility
import com.babilonia.presentation.flow.main.search.model.ListingsMetadata
import com.babilonia.presentation.view.priceview.BarEntry
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
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
    private val getPlaceByIdUseCase: GetPlaceByIdUseCase,
    private val getPlacesByQueryUseCase: GetPlacesByQueryUseCase,
    private val getCurrentPlaceUseCase: GetCurrentPlaceUseCase,
    private val getListingsMetadataUseCase: GetListingsMetadataUseCase,
    private val getPriceRangeUseCase: GetPriceRangeUseCase,
    private val getDefaultLocationUseCase: GetDefaultLocationUseCase,
    private val getFacilitiesUseCase: GetFacilitiesUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getTopListingsUseCase: GetTopListingsUseCase,
    private val appSystemProvider: AppSystemProvider,
    private val needToShowArOnboardingUseCase: NeedToShowArOnboardingUseCase
) : BaseViewModel(), ListingActionsListener, FiltersDelegate by FiltersDelegateImpl(), FacilityChangeListener {

    val topListingsLiveData = MutableLiveData<List<Listing>>()
    val listings = MutableLiveData<List<Listing>>()
    val locationLiveData = MutableLiveData<ILocation>()
    val priceRangeLiveData = MutableLiveData<List<BarEntry>>()
    val suggestions = MutableLiveData<List<Place>>()
    val recentSearches = MutableLiveData<List<RecentSearch>>()
    val onFocusChangeEvent = MutableLiveData<Boolean>()
    val filtersVisibilityLiveData = MutableLiveData<FiltersVisibility>(FiltersVisibility.All())
    val topListingsVisibilityLiveData = MutableLiveData<Boolean>(true)

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

    private var sessionToken = AutocompleteSessionToken.newInstance()

    var searchingByCurrentPlace = true
    var sortingBy = SortType.MOST_RELEVANT.ordinal
    var currentZoom = 15f
    var searchRadius = Constants.DEFAULT_RADIUS

    init {
        subscribeToListingMetadata()
    }

    override fun onFavouriteClicked(isChecked: Boolean, id: Long) {
        val mode = if (isChecked) {
            ListingActionMode.SET
        } else {
            ListingActionMode.DELETE
        }

        listingActionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(id, ListingAction.FAVOURITE, mode))
    }

    override fun onPreviewClicked(id: Long) {
        navigateGlobal(
            R.id.action_global_listingFragment,
            bundleOf(
                ID to id,
                MODE to ListingDisplayMode.IMPROPER_LISTING
            )
        )
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
        needToResetPaginator = true
        applyFilters(context.resources)
        navigateBack()
    }

    fun getListings() {
        val chosenLocation = locationLiveData.value
        if (chosenLocation != null) {
            getListings(chosenLocation)
        } else {
            getLocation()
        }
    }

    fun getListings(location: ILocation, radius: Int = searchRadius, pageSize: Int = Constants.PER_PAGE) {
        sessionToken = AutocompleteSessionToken.newInstance()
        disposables.clear()

        val disposable = paginator.onBackpressureDrop().subscribe { pageIndex ->
            getListingsUseCase.execute(object : DisposableSingleObserver<List<Listing>>() {
                override fun onSuccess(list: List<Listing>) {
                    lastLoadedPageIndex = pageIndex
                    updateTopListingsVisibility()
                    listings.postValue(list)
                    getRecentSearches()
                }

                override fun onError(e: Throwable) {
                    dataError.postValue(e)
                }

            }, GetListingsUseCase.Params(
                lat = location.latitude.toFloat(),
                lon = location.longitude.toFloat(),
                queryText = if (location.address.isNullOrEmpty()) null else location.address,
                placeId = if (location is PlaceLocation) location.googlePlaceId else null,
                page = pageIndex,
                radius = radius,
                sortType = SortType.getByPosition(sortingBy),
                filters = getFilters(),
                facilities = getFacilities(),
                pageSize = pageSize
            ))
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

    fun getListingsWithLocationPermission(isPermissionGranted: Boolean) {
        if (locationLiveData.value == null) {
            if (isPermissionGranted) {
                getLocation()
            } else {
                getDefaultPlace()
            }
        }
    }

    fun getLocation(onLocationFound: ((location: ILocation) -> Unit)? = null) {
        searchingByCurrentPlace = true
        getLocationUseCase.execute(object : DisposableSingleObserver<DataResult<ILocation>>() {
            override fun onSuccess(result: DataResult<ILocation>) {
                when (result) {
                    is DataResult.Error -> {
                        getDefaultPlace()
                        if (result.throwable is GpsUnavailableException) {
                            gpsUnavailableError.call()
                        } else {
                            dataError.postValue(result.throwable)
                        }
                    }
                    is DataResult.Success -> {
                        onLocationFound?.invoke(result.data)
                        locationLiveData.postValue(result.data)
                        metadataUpdateSubject.onNext(false)
                    }
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, Unit)
    }

    fun getDefaultPlace() {
        searchingByCurrentPlace = true
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(sessionToken)
            .setCountries(Constants.COUNTRY_CODE_PERU, Constants.COUNTRY_CODE_UKRAINE)
            .setTypeFilter(TypeFilter.REGIONS)
            .setQuery(Constants.LIMA_DEFAULT_REQUEST)
            .build()
        getPlacesByQueryUseCase.execute(object : DisposableSingleObserver<List<Place>>() {
            override fun onSuccess(places: List<Place>) {
                if (places.isNotEmpty()) {
                    getDefaultPlaceLocation(places[0].id)
                }
            }

            override fun onError(e: Throwable) {
                handleApiError(e)
            }

        }, request)
    }

    fun getFacilities(type: String) {
        getFacilitiesUseCase.execute(object : DisposableSubscriber<List<Facility>>() {
            override fun onComplete() {

            }

            override fun onNext(facilities: List<Facility>) {
                gotFacilitiesEvent.value = facilities
            }

            override fun onError(e: Throwable) {
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

    private fun getDefaultPlaceLocation(placeId: String) {
        getPlaceByIdUseCase.execute(object : DisposableSingleObserver<PlaceLocation>() {
            override fun onSuccess(location: PlaceLocation) {
                location.googlePlaceId = null
                safeLet(location.viewport?.center, location.viewport?.northeast) { center, bounds ->
                    searchRadius = calculateRadius(center, bounds)
                }
                currentPlace.value = location
                onPlaceFoundEvent.postValue(location)
                locationLiveData.postValue(location)
            }

            override fun onError(e: Throwable) {
                handleApiError(e)
            }

        }, placeId)
    }

    private fun handleApiError(e: Throwable) {
        val msg = e.message
        if (msg != null && msg.contains("NoConnectionError")) {
            dataError.postValue(NoNetworkException(e))
        } else {
            dataError.postValue(e)
        }
    }

    fun getPlaces(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountries(Constants.COUNTRY_CODE_PERU, Constants.COUNTRY_CODE_UKRAINE)
            .setTypeFilter(TypeFilter.REGIONS)
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()
        getPlacesByQueryUseCase.execute(object : DisposableSingleObserver<List<Place>>() {
            override fun onSuccess(places: List<Place>) {
                suggestions.postValue(places)
            }

            override fun onError(e: Throwable) {
                handleApiError(e)
            }

        }, request)
    }

    fun getPriceRange(shouldUseTmpFilters: Boolean = true) {
        if (shouldUseTmpFilters)  {
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
                        dataError.postValue(e)
                    }
                },
                GetListingsMetadataUseCase.Params(
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

    fun setLocationFromMapPosition(lat: Double, lon: Double, radius: Int) {
        val location = Location().apply {
            latitude = lat
            longitude = lon
        }
        locationLiveData.postValue(location)
    }

    fun getRecentSearches() {
        getRecentSearchesUseCase.execute(object : DisposableSingleObserver<List<RecentSearch>>(){
            override fun onSuccess(newRecentSearches: List<RecentSearch>) {
                recentSearches.postValue(newRecentSearches)
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    fun getPlaceById(googlePlaceId: String) {
        searchingByCurrentPlace = false
        getPlaceByIdUseCase.execute(object : DisposableSingleObserver<PlaceLocation>() {
            override fun onSuccess(location: PlaceLocation) {
                safeLet(location.viewport?.center, location.viewport?.northeast) { center, bounds ->
                    searchRadius = calculateRadius(center, bounds)
                }
                onPlaceFoundEvent.postValue(location)
                locationLiveData.postValue(location)
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, googlePlaceId)
    }

    fun getCurrentPlace() {
        searchingByCurrentPlace = true
        getCurrentPlaceUseCase.execute(object : DisposableSingleObserver<PlaceLocation>() {
            override fun onSuccess(location: PlaceLocation) {
                location.googlePlaceId = null
                searchRadius = Constants.DEFAULT_RADIUS
                onPlaceFoundEvent.postValue(location)
                currentPlace.postValue(location)
                locationLiveData.postValue(location)
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)

            }
        }, Unit)
    }

    fun getUserId() {
        getUserIdUseCase.execute(object : DisposableSingleObserver<Long>() {
            override fun onSuccess(userId: Long) {
                userIdLiveData.value = userId
            }

            override fun onError(e: Throwable) {
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
                    it.latitude.toFloat(),
                    it.longitude.toFloat(),
                    searchRadius,
                    filters,
                    facilities
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
                if (shouldUseTmpFilters)  {
                    filters = getTempFilters()
                    facilities = getTempFacilities()
                } else {
                    filters = getFilters()
                    facilities = getFacilities()
                }
                getListingsMetadata(filters, facilities)
                getPriceRange(filters, facilities)
            }
    }

    private fun calculateRadius(center: LatLng, bounds: LatLng): Int {
        val from = AndroidLocation(EmptyConstants.EMPTY_STRING).apply {
            latitude = center.latitude
            longitude = center.longitude
        }
        val to = AndroidLocation(EmptyConstants.EMPTY_STRING).apply {
            latitude = bounds.latitude
            longitude = bounds.longitude
        }
        val radius = from.distanceTo(to).toInt()
        return radius
    }

    private fun getDefaultLocation() {
        getDefaultLocationUseCase.execute(object : DisposableSingleObserver<Location>() {
            override fun onSuccess(location: Location) {
                locationLiveData.postValue(location)
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, Unit)
    }

    fun getTopListings(location: ILocation, radius: Int = Constants.MAX_RADIUS) {
        if (isTopListingVisible().not()) return

        getTopListingsUseCase.execute(object : DisposableSingleObserver<List<Listing>>(){
            override fun onSuccess(topListings: List<Listing>) {
                topListingsLiveData.value = topListings
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
                e.printStackTrace()
            }
        }, GetTopListingsUseCase.Params(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            radius
        ))
    }

    private fun updateTopListingsVisibility() {
        topListingsVisibilityLiveData.value = isTopListingVisible()
    }

    private fun isTopListingVisible() : Boolean {
        val visibleByFilters = hasFilters().not()
        return visibleByFilters && searchingByCurrentPlace
    }
}

