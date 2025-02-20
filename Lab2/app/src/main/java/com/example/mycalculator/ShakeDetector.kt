package com.example.mycalculator

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var isRegistered = false

    fun start(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null && !isRegistered) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            isRegistered = true
        }
    }

    fun stop() {
        if (isRegistered) {
            sensorManager.unregisterListener(this)
            isRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Условие для определения тряски
            if (Math.abs(x) > 15 || Math.abs(y) > 15 || Math.abs(z) > 15) {
                onShake() // Вызываем функцию очистки
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не требуется
    }
}