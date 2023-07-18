package com.babilonia.presentation.flow.main.listing.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.android.exceptions.GpsUnavailableException
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.CurrentLocationSubscriberUseCase
import com.babilonia.domain.usecase.GetListingUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class ListingFullscreenMapViewModel @Inject constructor(
    private val getListingUseCase: GetListingUseCase,
    private val locationUseCase: CurrentLocationSubscriberUseCase
) : BaseViewModel() {

    private val listingLiveData = MutableLiveData<Listing>()
    private val gpsUnavailableErrorLiveData = SingleLiveEvent<Unit>()
    var currentLocation: ILocation = Location(Constants.LIMA_LAT, Constants.LIMA_LON)
        private set

    private var wasUnavailableDialogShown = false

    fun getListing(id: Long) {
        getListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(listing: Listing) {
                listingLiveData.value = listing
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, GetListingUseCase.Params(id, ListingDisplayMode.IMPROPER_LISTING, true))
    }

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
                        is DataResult.Success -> {
                            currentLocation = result.data
                        }
                        is DataResult.Error -> {
                            if (result.throwable is GpsUnavailableException) {
                                if (wasUnavailableDialogShown.not()) {
                                    gpsUnavailableErrorLiveData.call()
                                    wasUnavailableDialogShown = true
                                }
                            } else {
                                dataError.postValue(result.throwable)
                            }
                        }
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

    fun unsubscribeFromMyLocation() {
        locationUseCase.clear()
    }

    fun getListingLiveData(): LiveData<Listing> = listingLiveData
    fun getGpsUnavailableErrorLiveData(): LiveData<Unit> = gpsUnavailableErrorLiveData
}