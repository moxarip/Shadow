package com.example.audio

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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Audio Manager that generates high-quality sound effects
 * using Android's native AudioTrack API without requiring external audio assets.
 * Also manages haptic tactile feedback.
 */
class GameAudioManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)

    var soundEnabled: Boolean = true
    var vibrationEnabled: Boolean = true

    private val sampleRate = 22050
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    enum class SoundType {
        CLICK,
        ATTACK,
        HIT,
        DASH,
        ABILITY_FIRE,
        ABILITY_ICE,
        ABILITY_LIGHTNING,
        ABILITY_HEAL,
        COIN,
        VICTORY,
        DEFEAT,
        LEVEL_UP,
        WARNING
    }

    fun playSound(type: SoundType) {
        if (!soundEnabled) return

        audioScope.launch {
            try {
                val pcmData = generateSoundPcm(type) ?: return@launch
                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(pcmData.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmData, 0, pcmData.size)
                track.play()
                // Let it play, then release
                val durationMs = (pcmData.size / 2 * 1000L) / sampleRate + 50
                Thread.sleep(durationMs)
                track.release()
            } catch (_: Exception) {
                // Silently handle audio hardware limitations
            }
        }
    }

    fun vibrate(durationMs: Long = 40, amplitude: Int = 180) {
        if (!vibrationEnabled) return
        try {
            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val clampedAmp = amplitude.coerceIn(1, 255)
                        it.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun generateSoundPcm(type: SoundType): ByteArray? {
        return when (type) {
            SoundType.CLICK -> generateToneSweep(startFreq = 880.0, endFreq = 1200.0, durationSec = 0.04f, decay = true)
            SoundType.ATTACK -> generateBladeWhoosh()
            SoundType.HIT -> generatePunchThud()
            SoundType.DASH -> generateToneSweep(startFreq = 400.0, endFreq = 950.0, durationSec = 0.12f, decay = true)
            SoundType.ABILITY_FIRE -> generateExplosionTone()
            SoundType.ABILITY_ICE -> generateChime(listOf(1046.5, 1318.5, 1567.98), 0.25f)
            SoundType.ABILITY_LIGHTNING -> generateZapTone()
            SoundType.ABILITY_HEAL -> generateArpeggio(listOf(523.25, 659.25, 783.99, 1046.5), 0.35f)
            SoundType.COIN -> generateChime(listOf(987.77, 1318.51), 0.12f)
            SoundType.VICTORY -> generateVictoryFanfare()
            SoundType.DEFEAT -> generateToneSweep(startFreq = 380.0, endFreq = 110.0, durationSec = 0.45f, decay = true)
            SoundType.LEVEL_UP -> generateArpeggio(listOf(440.0, 554.37, 659.25, 880.0), 0.4f)
            SoundType.WARNING -> generateToneSweep(startFreq = 300.0, endFreq = 220.0, durationSec = 0.15f, decay = false)
        }
    }

    private fun generateToneSweep(startFreq: Double, endFreq: Double, durationSec: Float, decay: Boolean): ByteArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sampleRate
            val envelope = if (decay) (1.0 - progress).coerceAtLeast(0.0) else 1.0
            val sample = (sin(phase) * 30000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generateBladeWhoosh(): ByteArray {
        val numSamples = (0.10f * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 600.0 - 400.0 * progress
            phase += 2.0 * PI * freq / sampleRate
            val noise = (Math.random() * 2 - 1) * 0.4
            val envelope = sin(progress * PI)
            val sample = ((sin(phase) * 0.6 + noise) * 26000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generatePunchThud(): ByteArray {
        val numSamples = (0.08f * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 160.0 - 100.0 * progress
            phase += 2.0 * PI * freq / sampleRate
            val noise = (Math.random() * 2 - 1) * 0.5
            val envelope = (1.0 - progress) * (1.0 - progress)
            val sample = ((sin(phase) * 0.7 + noise * 0.3) * 32000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generateExplosionTone(): ByteArray {
        val numSamples = (0.28f * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 120.0 - 80.0 * progress
            phase += 2.0 * PI * freq / sampleRate
            val noise = (Math.random() * 2 - 1) * 0.8
            val envelope = (1.0 - progress) * (1.0 - progress)
            val sample = ((sin(phase) * 0.4 + noise * 0.6) * 31000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generateZapTone(): ByteArray {
        val numSamples = (0.16f * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 1400.0 + sin(progress * 40) * 600.0
            phase += 2.0 * PI * freq / sampleRate
            val envelope = (1.0 - progress)
            val sample = (sin(phase) * 28000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generateChime(freqs: List<Double>, durationSec: Float): ByteArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        val phases = DoubleArray(freqs.size) { 0.0 }

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress)
            var sum = 0.0
            for (fIdx in freqs.indices) {
                phases[fIdx] += 2.0 * PI * freqs[fIdx] / sampleRate
                sum += sin(phases[fIdx])
            }
            val sample = ((sum / freqs.size) * 30000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generateArpeggio(notes: List<Double>, totalDurationSec: Float): ByteArray {
        val numSamples = (totalDurationSec * sampleRate).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        val noteLength = numSamples / notes.size
        var phase = 0.0

        for (i in 0 until numSamples) {
            val noteIdx = (i / noteLength).coerceAtMost(notes.size - 1)
            val localProgress = (i % noteLength).toDouble() / noteLength
            val freq = notes[noteIdx]
            phase += 2.0 * PI * freq / sampleRate
            val envelope = 1.0 - localProgress * 0.4
            val sample = (sin(phase) * 26000 * envelope).toInt().toShort()
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    private fun generateVictoryFanfare(): ByteArray {
        val chords = listOf(523.25, 659.25, 783.99, 1046.5)
        return generateArpeggio(chords, 0.6f)
    }
}
