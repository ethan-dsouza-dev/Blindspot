package com.blindspot.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Streams the device's current heading (azimuth) in degrees clockwise from true/magnetic north,
 * derived from the rotation-vector sensor. Emits a smoothed value to reduce jitter.
 */
class CompassSensorManager(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val hasSensor: Boolean get() = rotationVectorSensor != null

    /**
     * Emits the device azimuth in degrees [0, 360). Low-pass filtered for smoothness.
     */
    fun headingDegrees(): Flow<Float> = callbackFlow {
        val sensor = rotationVectorSensor
        if (sensor == null) {
            // No compass hardware; close without emitting so callers can fall back gracefully.
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var smoothed: Float? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthDeg = GeoUtils.normalizeDegrees(
                    Math.toDegrees(orientation[0].toDouble()).toFloat()
                )
                smoothed = lowPass(azimuthDeg, smoothed)
                trySend(smoothed!!)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_UI,
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /**
     * Angular low-pass filter that correctly handles the 0/360 wrap-around.
     */
    private fun lowPass(newValue: Float, previous: Float?, alpha: Float = 0.35f): Float {
        if (previous == null) return newValue
        var delta = newValue - previous
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return GeoUtils.normalizeDegrees(previous + alpha * delta)
    }
}
