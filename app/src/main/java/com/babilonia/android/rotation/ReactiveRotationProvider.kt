package com.babilonia.android.rotation

import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.view.Surface
import android.view.WindowManager
import io.reactivex.Observable

class ReactiveRotationProvider(
    private val sensorManager: SensorManager,
    private val windowManager: WindowManager
) : RotationObservable() {

    override fun getSensorManager() = sensorManager
    override fun getWindowsManager() = windowManager

    /**
     * The app uses only portrait orientation, so the app doesn't need to handle screen orientation
     * In this case, the remapping of the coordinate system is not needed
     */
    fun subscribeToWindowRotation(): Observable<Pair<FloatArray, FloatArray>> {
        return getRotationObservable()
            .map { rotationData ->
                val rotationMatrixFromVector = FloatArray(16)

                val rotationMatrix = FloatArray(16)
                val mapRotationMatrix = FloatArray(16)

                getRotationMatrixFromVector(rotationMatrixFromVector, rotationData.values)

                val (xAxis, yAxis) = when (getWindowsManager().defaultDisplay.rotation) {
                    Surface.ROTATION_90 -> AXIS_Y to AXIS_MINUS_X
                    Surface.ROTATION_270 -> AXIS_MINUS_Y to AXIS_X
                    Surface.ROTATION_180 -> AXIS_MINUS_X to AXIS_MINUS_Y
                    else -> AXIS_X to AXIS_Y
                }

                remapCoordinateSystem(rotationMatrixFromVector, xAxis, yAxis, rotationMatrix)
                remapCoordinateSystem(rotationMatrixFromVector, AXIS_X, AXIS_Z, mapRotationMatrix)

                rotationMatrix to mapRotationMatrix
            }
    }

    private fun getRotationObservable() = Observable.create<RotationData>(
        ReactiveRotationProvider(
            sensorManager,
            windowManager
        )
    )
}

