package com.babilonia.android.system

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.babilonia.data.datasource.system.SystemProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AppSystemProvider @Inject constructor(val context: Context) : SystemProvider {

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun isLocationEnabledSingle(): Single<Boolean> {
        return Single.create { emitter ->
            emitter.onSuccess(isLocationEnabled())
        }
    }

    override fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(): Single<Location?> {
        return Single.create<Location> {  emitter ->
            locationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    emitter.onSuccess(location)
                } else {
                    emitter.onError(IllegalArgumentException())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long, minDistance: Float): Observable<Location> {
        val subject = PublishSubject.create<Location>()
        val locationListener = getLocationListener(subject)
        return subject.doOnSubscribe {
            locationListener?.let { it1 ->
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    interval,
                    minDistance,
                    it1
                )
            }
        }.doOnDispose {
            locationListener?.let { locationManager.removeUpdates(it) }
        }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun getLocationListener(subject: PublishSubject<Location>): LocationListener? {
        return object : LocationListener {
                override fun onLocationChanged(location: Location) {
                   subject.onNext(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }
            }
    }
}