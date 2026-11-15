package com.grupo3.compras.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.grupo3.compras.domain.Actividad
import kotlin.math.sqrt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Detecta si el usuario camina usando solo el acelerómetro (no requiere permisos ni Internet).
 * Mide cuánto varía la magnitud de la aceleración en una ventana de muestras: al caminar varía mucho.
 */
class DetectorActividad(context: Context) {
    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun disponible(): Boolean = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    fun actividad(): Flow<Actividad> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        trySendBlocking(Actividad.SENTADO)
        if (sensor == null) {
            awaitClose { }
        } else {
            val muestras = ArrayDeque<Float>()
            var actual = Actividad.SENTADO
            var desdeUltimoCalculo = 0
            val listener = object : SensorEventListener {
                override fun onSensorChanged(evento: SensorEvent) {
                    val x = evento.values[0]
                    val y = evento.values[1]
                    val z = evento.values[2]
                    muestras.addLast(sqrt(x * x + y * y + z * z))
                    if (muestras.size > VENTANA) muestras.removeFirst()
                    desdeUltimoCalculo++
                    if (muestras.size >= VENTANA_MINIMA && desdeUltimoCalculo >= CADA_N_MUESTRAS) {
                        desdeUltimoCalculo = 0
                        val media = muestras.average()
                        val varianza = muestras.sumOf { val d = it - media; d * d } / muestras.size
                        val desviacion = sqrt(varianza)
                        // Histéresis: evita que el estado parpadee entre caminando y sentado.
                        val nueva = when {
                            desviacion > UMBRAL_CAMINANDO -> Actividad.CAMINANDO
                            desviacion < UMBRAL_QUIETO -> Actividad.SENTADO
                            else -> actual
                        }
                        if (nueva != actual) {
                            actual = nueva
                            trySendBlocking(nueva)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }

    private companion object {
        const val VENTANA = 100
        const val VENTANA_MINIMA = 50
        const val CADA_N_MUESTRAS = 25
        const val UMBRAL_CAMINANDO = 1.0
        const val UMBRAL_QUIETO = 0.5
    }
}
