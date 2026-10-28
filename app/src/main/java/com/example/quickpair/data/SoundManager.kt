package com.example.quickpair.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sampleRate = 44100

    private fun playTone(
        frequencies: List<Pair<Double, Int>>, // frequency in Hz, duration in ms
        gain: Float = 0.45f
    ) {
        scope.launch {
            try {
                var totalDurationMs = 0
                for (pair in frequencies) {
                    totalDurationMs += pair.second
                }
                val totalSamples = (sampleRate * totalDurationMs / 1000.0).toInt()
                if (totalSamples <= 0) return@launch

                val buffer = ShortArray(totalSamples)
                var currentSample = 0

                for ((freq, durationMs) in frequencies) {
                    val count = (sampleRate * durationMs / 1000.0).toInt()
                    for (i in 0 until count) {
                        if (currentSample >= totalSamples) break
                        // Smooth attack and release envelope to prevent clicking
                        val envelope = when {
                            i < 100 -> i / 100f
                            i > count - 150 -> (count - i) / 150f
                            else -> 1f
                        }
                        val angle = 2.0 * PI * i / (sampleRate / freq)
                        val sampleValue = (sin(angle) * Short.MAX_VALUE * gain * envelope).toInt()
                        buffer[currentSample++] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }

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

                audioTrack.write(buffer, 0, currentSample)
                audioTrack.play()

                // Release track after playback finished
                launch {
                    kotlinx.coroutines.delay(totalDurationMs.toLong() + 50L)
                    audioTrack.stop()
                    audioTrack.release()
                }
            } catch (_: Exception) {
                // Ignore audio error gracefully
            }
        }
    }

    fun playCountdownTick(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(listOf(Pair(784.0, 40)), gain = 0.35f)
    }

    fun playCountdownGo(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(listOf(Pair(1046.5, 90)), gain = 0.5f)
    }

    fun playTileSelect(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(listOf(Pair(620.0, 25)), gain = 0.3f)
    }

    fun playMatchSuccess(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(
            listOf(
                Pair(587.33, 60), // D5
                Pair(880.00, 100) // A5
            ),
            gain = 0.45f
        )
    }

    fun playMatchWrong(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(
            listOf(
                Pair(260.0, 70),
                Pair(190.0, 90)
            ),
            gain = 0.4f
        )
    }

    fun playTimeWarning(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(listOf(Pair(920.0, 35)), gain = 0.4f)
    }

    fun playBoardClear(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(
            listOf(
                Pair(523.25, 60),  // C5
                Pair(659.25, 60),  // E5
                Pair(783.99, 60),  // G5
                Pair(1046.50, 140) // C6
            ),
            gain = 0.5f
        )
    }

    fun playStreakMilestone(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(
            listOf(
                Pair(659.25, 70),  // E5
                Pair(880.00, 70),  // A5
                Pair(1318.51, 140) // E6
            ),
            gain = 0.45f
        )
    }

    fun playGameOver(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(
            listOf(
                Pair(440.00, 100), // A4
                Pair(370.00, 100), // F#4
                Pair(293.66, 160)  // D4
            ),
            gain = 0.45f
        )
    }

    // Haptics implementation
    fun vibrateSelection(hapticsEnabled: Boolean) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun vibrateSuccess(hapticsEnabled: Boolean) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 40, 40), -1)
            }
        } catch (_: Exception) {}
    }

    fun vibrateWarning(hapticsEnabled: Boolean) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 45, 50, 45), -1)
            }
        } catch (_: Exception) {}
    }

    fun vibrateGameOver(hapticsEnabled: Boolean) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(120)
            }
        } catch (_: Exception) {}
    }
}
