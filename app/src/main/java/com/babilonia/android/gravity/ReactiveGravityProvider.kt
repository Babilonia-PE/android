package com.babilonia.android.gravity

import android.hardware.SensorManager
import com.babilonia.android.rotation.LowPassFilter
import io.reactivex.Observable

class ReactiveGravityProvider(
    lowPassFilter: LowPassFilter,
    private val sensorManager: SensorManager
) : GravityObservable(lowPassFilter) {

    override fun getSensorManager() = sensorManager

    fun subscribeToAccelerometer(): Observable<Float> {
        return getGravityDataObservable()
            .map { it.xGravity }
    }

    private fun getGravityDataObservable() = Observable.create<GravityData>(
        ReactiveGravityProvider(
            lowPassFilter, sensorManager
        )
    )
}