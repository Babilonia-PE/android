package com.babilonia.android.gravity

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.babilonia.android.rotation.LowPassFilter
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables
import java.util.concurrent.TimeUnit


abstract class GravityObservable(protected val lowPassFilter: LowPassFilter) :
    ObservableOnSubscribe<GravityObservable.GravityData> {

    companion object {
        private val TIME_THRESHOLD_NS = TimeUnit.MILLISECONDS.toNanos(0)
        private val HAND_DOWN_GRAVITY_X_THRESHOLD = -.040f
        private val HAND_UP_GRAVITY_X_THRESHOLD = -.040f
    }

    private var lastEventTime: Long = 0
    private var initialGravity = -100F

    protected abstract fun getSensorManager(): SensorManager

    override fun subscribe(emitter: ObservableEmitter<GravityData>) {
        val sensorCallBacks = AccelerometerUpdateListener(emitter)

        val sensorManager = getSensorManager()
        val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorManager.registerListener(sensorCallBacks, rotation, SensorManager.SENSOR_DELAY_NORMAL)
    }

    inner class AccelerometerUpdateListener(private val emitter: ObservableEmitter<GravityData>) :
        SensorEventListener {

        init {
            emitter.setDisposable(Disposables.fromAction {
                initialGravity = -100F
                getSensorManager().unregisterListener(this)
            })
        }

        override fun onSensorChanged(event: SensorEvent) {
            detectJump(event.values, event.timestamp, emitter)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Empty
        }
    }

    // private

    /**
     * A very simple algorithm to detect a successful up-down movement of hand(s). The algorithm
     * is based on a delta of the handing being up vs. down and taking less than TIME_THRESHOLD_NS
     * to happen.
     *
     *
     * This algorithm isn't intended to be used in production but just to show what's possible with
     * sensors. You will want to take into account other components (y and z) and other sensors to
     * get a more accurate reading.
     */

    private fun detectJump(
        coordinates: FloatArray,
        timestamp: Long,
        emitter: ObservableEmitter<GravityData>
    ) {
//        Log.d("detectJump", "${coordinates[0]} ${coordinates[1]}  ${coordinates[2]}")

        val xGravity = coordinates[0]

        if (initialGravity == -100F) {
            initialGravity = xGravity
        }

//        val jump = Math.sqrt(Math.pow(coordinates[0].toDouble(), 2.0) + Math.pow(coordinates[1].toDouble(), 2.0) +
//                Math.pow(coordinates[2].toDouble(), 2.0)).toFloat()

        if ((xGravity < HAND_DOWN_GRAVITY_X_THRESHOLD) || (xGravity >= HAND_UP_GRAVITY_X_THRESHOLD)) {

            if (timestamp - lastEventTime > TIME_THRESHOLD_NS) {
                // Hand is down when yValue is negative.
                if (!emitter.isDisposed) {
//                    Log.d(
//                        "detectJump",
//                        "initial gravity=  ${initialGravity} xGravityDelta = ${xGravity - initialGravity} ${xGravity > initialGravity}"
//                    )
//                    val delta = Math.abs(xGravity * xGravity - initialGravity * initialGravity)

//                    Log.d("detectJump", "delta = ${delta}")
                    emitter.onNext(
                        GravityData(
                            ((xGravity * 10) * (xGravity * 10) - (initialGravity * 10) * (initialGravity * 10)),
                            xGravity > initialGravity
                        )
                    )
                }
            }

            lastEventTime = timestamp
        }
    }

    data class GravityData(val xGravity: Float, val handUp: Boolean)
}
