package com.babilonia.android.gravity

import android.hardware.SensorManager
import com.babilonia.android.rotation.LowPassFilter
import com.babilonia.data.datasource.system.GravityProvider
import io.reactivex.Observable
import javax.inject.Inject

class AppGravityProvider @Inject constructor(
    private val sensorManager: SensorManager,
    private val lowPassFilter: LowPassFilter
) : GravityProvider {

    override fun getGravity(): Observable<Float> {
        return ReactiveGravityProvider(lowPassFilter, sensorManager)
            .subscribeToAccelerometer()

    }
}