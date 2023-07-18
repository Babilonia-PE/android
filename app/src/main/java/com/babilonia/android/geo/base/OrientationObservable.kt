package com.babilonia.android.geo.base

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables


/** Use this class to get device orientation params : orientation contains: azimut, pitch and roll */

abstract class OrientationObservable : ObservableOnSubscribe<FloatArray> {

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private val mR = FloatArray(9)
    private val orientation = FloatArray(3)

    protected abstract fun getSensorManager(): SensorManager

    override fun subscribe(emitter: ObservableEmitter<FloatArray>) {
        val sensorCallBacks = SensorUpdateListener(emitter)

        val sensorManager = getSensorManager()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(sensorCallBacks, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorCallBacks, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    inner class SensorUpdateListener(private val emitter: ObservableEmitter<FloatArray>) : SensorEventListener {

        init {
            emitter.setDisposable(Disposables.fromAction {
                getSensorManager().unregisterListener(this)
            })
        }

        override fun onSensorChanged(event: SensorEvent) {

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                }
                else -> {
                    // empty
                }
            }
            if (lastAccelerometerSet && lastMagnetometerSet && !emitter.isDisposed) {
                SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnetometer)
                SensorManager.getOrientation(mR, orientation)
//                Log.d(
//                    "OrientationObservable",
//                    "Orientation: %f, %f, %f ${orientation[0]} ${orientation[1]} ${orientation[2]} "
//                )
                emitter.onNext(orientation)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Empty
        }
    }
}