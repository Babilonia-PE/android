package com.babilonia.presentation.flow.main.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.android.geo.AppOrientationProvider
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.CurrentLocationSubscriberUseCase
import com.babilonia.domain.usecase.GetListingUseCase
import com.babilonia.domain.usecase.GetRouteUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.extension.safeLet
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlin.math.abs

class FullscreenMapViewModel @Inject constructor(
    private val getListingUseCase: GetListingUseCase,
    private val locationUseCase: CurrentLocationSubscriberUseCase,
    private val getRouteUseCase: GetRouteUseCase,
    private val orientationProvider: AppOrientationProvider
) : BaseViewModel() {
    val authFailedData = SingleLiveEvent<Unit>()
    private val listingLiveData = MutableLiveData<Listing>()
    private val currentLocationLiveData = MutableLiveData<ILocation>()
    private val routeLiveData = MutableLiveData<List<RouteStep>>()
    private val orientationLiveData = MutableLiveData<Float>()

    fun subscribeToMyLocation() {
        locationUseCase.subscribe(
            object : DisposableObserver<DataResult<ILocation>>() {

                override fun onComplete() {
                    // empty
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onNext(result: DataResult<ILocation>) {
                    when (result) {
                        is DataResult.Success -> currentLocationLiveData.postValue(result.data)
                        is DataResult.Error -> result.throwable.printStackTrace()
                    }
                }
            }, CurrentLocationSubscriberUseCase.Params(
                LocationRequest(
                    LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                    LocationRequest.LOCATION_UPDATE_INTERVAL_LARGE
                )
            )
        )
    }

    fun subscribeToOrientation() {
        disposables.add(
            orientationProvider.getDeviceOrientation()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {  orientationVector ->
                var newValue = Math.toDegrees(orientationVector[0].toDouble()).toFloat()
                orientationLiveData.value?.let { oldValue ->
                    // Orientation values are in [-180..180] bounds. We need to find difference and
                    // pass it through a kind of low-pass filter to avoid arrow trembling.
                    val lowPassFilterValue = 0.95f
                    val diff = if (abs(oldValue - newValue) > 180f) {
                        // special case when we rotate e.g. from 175 to -175 degrees - difference is
                        // 10 degrees but low pass filter will work not as expected
                        val multiplier = if (newValue > 0f) 1f else -1f
                        (360f - abs(oldValue) - abs(newValue)) * multiplier
                    } else {
                        // usual case when we rotate e.g. from 30 to 40 degrees
                        oldValue - newValue
                    }
                    newValue += lowPassFilterValue * diff
                }

                orientationLiveData.value = newValue
            }
        )
    }

    fun getListing(listingId: Long) {
        getListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(listing: Listing) {
                listingLiveData.value = listing
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.postValue(e)
            }
        }, GetListingUseCase.Params(
            id = listingId,
            mode = ListingDisplayMode.IMPROPER_LISTING,
            local = true
        ))
    }

    fun getRoute() {
        safeLet(currentLocationLiveData.value, listingLiveData.value?.locationAttributes) {
                currentLocation, listingLocation ->
            getRouteUseCase.execute(object : DisposableSingleObserver<List<RouteStep>>() {
                override fun onSuccess(route: List<RouteStep>) {
                    routeLiveData.value = route
                }

                override fun onError(e: Throwable) {
                    dataError.postValue(e)
                }
            }, GetRouteUseCase.Params(currentLocation, listingLocation))
        }
    }

    fun getListingLiveData(): LiveData<Listing> = listingLiveData
    fun getCurrentLocationLiveData(): LiveData<ILocation> = currentLocationLiveData
    fun getRouteLiveData(): LiveData<List<RouteStep>> = routeLiveData
    fun getOrientationLiveData(): LiveData<Float> = orientationLiveData
}