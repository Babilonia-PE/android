package com.babilonia.android.geo

import android.hardware.SensorManager
import com.babilonia.data.datasource.system.OrientationProvider
import io.reactivex.Observable
import javax.inject.Inject

class AppOrientationProvider @Inject constructor(private val sensorManager: SensorManager) : OrientationProvider {

    override fun getDeviceOrientation(): Observable<FloatArray> {
        return ReactiveOrientationProvider(sensorManager).getUpdatedOrientation()
    }
}