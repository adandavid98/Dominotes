package com.example.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class DominoSoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted: Boolean = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isSoundEnabled(): Boolean = !isMuted

    /**
     * Golpe seco y realista de una ficha de dominó sobre mesa de madera.
     * Síntesis acústica de impacto transitorio cerámico/hueso + resonancia de madera amortiguada.
     */
    fun playTileClack(isDecisive: Boolean = false) {
        if (isMuted) return
        triggerHaptic(if (isDecisive) 45 else 20)

        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = if (isDecisive) 180 else 120
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                val clickFreq = if (isDecisive) 1200.0 else 1650.0 + Random.nextDouble(-100.0, 100.0)
                val woodBodyFreq = if (isDecisive) 160.0 else 220.0 + Random.nextDouble(-20.0, 20.0)
                val woodDecay = if (isDecisive) 18.0 else 28.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Impact spike (transiente de choque agudo en los primeros 15ms)
                    val click = sin(2.0 * PI * clickFreq * t) * exp(-140.0 * t)
                    // Resonancia de cuerpo de madera (onda grave amortiguada)
                    val body = sin(2.0 * PI * woodBodyFreq * t) * exp(-woodDecay * t)
                    // Ruido blanco percusivo en micro-milisegundos
                    val noise = (Random.nextDouble(-0.3, 0.3)) * exp(-200.0 * t)

                    val mixed = (click * 0.45 + body * 0.50 + noise * 0.05)
                    val amplitude = if (isDecisive) 28000.0 else 22000.0
                    buffer[i] = (mixed * amplitude).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {
                // Ignore audio failure on restricted background or emulators
            }
        }
    }

    /**
     * Golpe seco fuerte sobre la mesa (¡Tranca! o ¡Ganador de Ronda!).
     */
    fun playHeavySlam() {
        if (isMuted) return
        triggerHaptic(60)

        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 240
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val impact = sin(2.0 * PI * 950.0 * t) * exp(-90.0 * t)
                    val subBass = sin(2.0 * PI * 130.0 * t) * exp(-12.0 * t)
                    val woodTable = sin(2.0 * PI * 280.0 * t) * exp(-22.0 * t)

                    val mixed = impact * 0.35 + subBass * 0.45 + woodTable * 0.20
                    buffer[i] = (mixed * 31000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Doble toque seco sobre la mesa indicando '¡Paso!'.
     */
    fun playPassTap() {
        if (isMuted) return
        triggerHaptic(15)

        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 220
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                val tap1Samples = (sampleRate * 0.08).toInt()
                val tap2Offset = (sampleRate * 0.09).toInt()

                for (i in 0 until numSamples) {
                    var sampleVal = 0.0
                    // Tap 1
                    if (i < tap1Samples) {
                        val t = i.toDouble() / sampleRate
                        sampleVal += sin(2.0 * PI * 340.0 * t) * exp(-45.0 * t)
                    }
                    // Tap 2 (más suave)
                    if (i >= tap2Offset) {
                        val t = (i - tap2Offset).toDouble() / sampleRate
                        sampleVal += sin(2.0 * PI * 360.0 * t) * exp(-50.0 * t) * 0.85
                    }
                    buffer[i] = (sampleVal * 20000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        // Release after finish
        scope.launch {
            kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 50L)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
