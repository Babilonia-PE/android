package com.babilonia.android.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.babilonia.android.exceptions.GpsUnavailableException
import com.babilonia.android.exceptions.UserLocationNotFoundException
import com.babilonia.data.datasource.system.LocationProvider
import com.babilonia.data.datasource.system.SystemProvider
import com.babilonia.data.model.DataResult
import com.babilonia.data.model.geo.LocationRequest
import com.babilonia.domain.model.geo.ILocation
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppLocationProvider @Inject constructor(
    val context: Context,
    private val systemProvider: SystemProvider
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(): Single<DataResult<ILocation>> {
        return systemProvider.getLastKnownLocation().flatMap<DataResult<ILocation>> { location: Location ->
            Single.just(DataResult.Success(AndroidLocation(location)))
        }.onErrorResumeNext {
            systemProvider.isLocationEnabledSingle().map<DataResult<ILocation>> { isGpsEnabled ->
                if (isGpsEnabled) {
                    DataResult.Error(UserLocationNotFoundException())
                } else {
                    DataResult.Error(GpsUnavailableException())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun getUpdatedLocation(locationRequest: LocationRequest): Observable<DataResult<ILocation>> {
        return Observable.combineLatest(
            getUpdatedLocationObservable(locationRequest),
            getGpsAvailableObservable().observeOn(Schedulers.io()), BiFunction { result, enabled ->
                when (enabled) {
                    true -> result
                    else -> DataResult.Error(GpsUnavailableException())
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun getUpdatedLocationObservable(locationRequest: LocationRequest): Observable<DataResult<ILocation>> {
        return Observable
            .concat(
                getLastKnownLocation().toObservable(),
                systemProvider.getLocationUpdates(locationRequest.interval, LOCATION_REFRESH_DISTANCE)
                    .observeOn(Schedulers.io())
                    .map { DataResult.Success(AndroidLocation(it)) }
            )
    }

    private fun getGpsAvailableObservable(): Observable<Boolean> {
        return Observable.interval(0, 5, TimeUnit.SECONDS)
            .flatMapSingle {
                systemProvider.isLocationEnabledSingle()
            }
    }

    companion object {
        private const val LOCATION_REFRESH_DISTANCE = 7f
    }
}
