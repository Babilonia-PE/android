package com.babilonia.android.rotation

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import android.view.WindowManager
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables

abstract class RotationObservable : ObservableOnSubscribe<RotationObservable.RotationData> {

    protected abstract fun getSensorManager(): SensorManager
    protected abstract fun getWindowsManager(): WindowManager

    //TODO use injection for it
    private val lowPassFilter = LowPassFilter(0.15f)
    private var rotationValues: FloatArray? = null

    override fun subscribe(emitter: ObservableEmitter<RotationData>) {
        val sensorCallBacks = RotationUpdateListener(emitter)

        val sensorManager = getSensorManager()
        val rotation = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)

        sensorManager.registerListener(sensorCallBacks, rotation, SENSOR_DELAY_UI)
    }

    inner class RotationUpdateListener(private val emitter: ObservableEmitter<RotationData>) : SensorEventListener {

        init {
            emitter.setDisposable(Disposables.fromAction {
                getSensorManager().unregisterListener(this)
            })
        }

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                TYPE_ROTATION_VECTOR -> {
                    if (!emitter.isDisposed) {
                        with(rotationValues) {
                            if (this != null) {
                                lowPassFilter.applyFilter(event.values, this)
                                emitter.onNext(RotationData(this, getWindowsManager().defaultDisplay.rotation))
                            } else {
                                rotationValues = event.values
                            }
                        }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Empty
        }
    }

    data class RotationData(val values: FloatArray, val rotation: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RotationData

            if (!values.contentEquals(other.values)) return false
            if (rotation != other.rotation) return false

            return true
        }

        override fun hashCode(): Int {
            var result = values.contentHashCode()
            result = 31 * result + rotation
            return result
        }
    }
}