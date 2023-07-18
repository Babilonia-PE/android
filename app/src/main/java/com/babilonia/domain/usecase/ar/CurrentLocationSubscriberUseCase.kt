package com.babilonia.domain.usecase.ar

import com.babilonia.data.datasource.system.LocationProvider
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.domain.usecase.base.SubscriberUseCase
import io.reactivex.Observable
import javax.inject.Inject

class CurrentLocationSubscriberUseCase @Inject constructor(
    private val locationProvider: LocationProvider
) : SubscriberUseCase<DataResult<ILocation>, CurrentLocationSubscriberUseCase.Params>() {


    override fun buildSubscriptionUseCase(params: Params): Observable<DataResult<ILocation>> {
        return locationProvider.getUpdatedLocation(params.locationRequest)
    }


    class Params(
        val locationRequest: LocationRequest
    )
}