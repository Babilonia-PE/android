package com.babilonia.data.datasource.system

import com.babilonia.data.model.DataResult
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.domain.model.geo.ILocation
import io.reactivex.Observable
import io.reactivex.Single

interface LocationProvider {

    fun getLastKnownLocation(): Single<DataResult<ILocation>>
    fun getUpdatedLocation(locationRequest: LocationRequest): Observable<DataResult<ILocation>>
}