package com.heartsyncradio.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class MovementDetector(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val _isMoving = MutableStateFlow(false)
    val isMoving: StateFlow<Boolean> = _isMoving.asStateFlow()

    private val _movementDetectedDuringSong = MutableStateFlow(false)
    val movementDetectedDuringSong: StateFlow<Boolean> = _movementDetectedDuringSong.asStateFlow()

    // Rolling window of recent magnitudes (SENSOR_DELAY_NORMAL ~200ms per sample)
    private val recentMagnitudes = ArrayDeque<Double>()

    companion object {
        // Linear acceleration at rest: ~0.0-0.1 m/s²
        // Minor fidget: ~0.3-0.5, walking: >1.0
        const val MOVEMENT_THRESHOLD = 0.5
        // 10 samples at ~200ms = ~2 second rolling window
        const val WINDOW_SIZE = 10
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        _isMoving.value = false
        recentMagnitudes.clear()
    }

    fun resetSongMovement() {
        _movementDetectedDuringSong.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return

        val x = event.values[0].toDouble()
        val y = event.values[1].toDouble()
        val z = event.values[2].toDouble()
        val magnitude = sqrt(x * x + y * y + z * z)

        if (recentMagnitudes.size >= WINDOW_SIZE) {
            recentMagnitudes.removeFirst()
        }
        recentMagnitudes.addLast(magnitude)

        // Rolling average smooths out single spikes (notification buzz, etc.)
        val moving = recentMagnitudes.size >= 3 && recentMagnitudes.average() > MOVEMENT_THRESHOLD
        _isMoving.value = moving
        if (moving) {
            _movementDetectedDuringSong.value = true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
