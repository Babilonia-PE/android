package com.babilonia.android.rotation

import android.hardware.SensorManager
import android.view.WindowManager
import com.babilonia.data.datasource.system.RotationProvider
import io.reactivex.Observable
import javax.inject.Inject

class AppWindowRotationProvider @Inject constructor(
    private val sensorManager: SensorManager,
    private val windowManager: WindowManager
) : RotationProvider {

    override fun getWindowRotation(): Observable<Pair<FloatArray, FloatArray>> {
        return ReactiveRotationProvider(sensorManager, windowManager).subscribeToWindowRotation()
    }
}