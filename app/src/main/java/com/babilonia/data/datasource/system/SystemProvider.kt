package com.babilonia.data.datasource.system

import android.location.Location
import io.reactivex.Observable
import io.reactivex.Single

interface SystemProvider {
    fun isLocationEnabled(): Boolean
    fun isLocationEnabledSingle(): Single<Boolean>
    fun getLastKnownLocation(): Single<Location?>
    fun getLocationUpdates(interval: Long, minDistance: Float): Observable<Location>
}