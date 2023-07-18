package com.babilonia.android.geo

import android.hardware.SensorManager
import com.babilonia.android.geo.base.OrientationObservable
import io.reactivex.Observable


class ReactiveOrientationProvider(private val sensorManager: SensorManager) : OrientationObservable() {

    fun getUpdatedOrientation() = Observable.create<FloatArray>(
        ReactiveOrientationProvider(
            sensorManager
        )
    )

    override fun getSensorManager(): SensorManager {
        return sensorManager
    }

}