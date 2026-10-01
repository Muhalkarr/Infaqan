package com.example.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * Detektor gestur goyangan ponsel (Shake Detection) berbasis akselerometer.
 * Dilengkapi filtering threshold percepatan gravitasi (g-force) dan debounce
 * agar tidak memicu deteksi palsu saat ponsel diletakkan atau digerakkan biasa.
 */
class ShakeDetector(
    private val thresholdG: Float = 2.4f,
    private val debounceMs: Long = 1200L,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTimestamp: Long = 0L
    private var isListening: Boolean = false

    fun start(context: Context): Boolean {
        if (isListening) return true
        return try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null) {
                sensorManager?.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI
                )
                isListening = true
                Log.d(TAG, "Sensor ShakeDetector aktif.")
                true
            } else {
                Log.w(TAG, "Akselerometer tidak tersedia pada perangkat ini.")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memulai ShakeDetector: ${e.message}", e)
            false
        }
    }

    fun stop() {
        if (!isListening) return
        try {
            sensorManager?.unregisterListener(this)
            isListening = false
            Log.d(TAG, "Sensor ShakeDetector dinonaktifkan.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menonaktifkan ShakeDetector: ${e.message}", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Normalisasi ke g-force (1.0 = diam terkena gravitasi bumi)
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > thresholdG) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= debounceMs) {
                lastShakeTimestamp = now
                Log.i(TAG, "Goyangan ponsel terdeteksi! G-Force: $gForce")
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    companion object {
        private const val TAG = "ShakeDetector"
    }
}
