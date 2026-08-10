package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.sin

enum class AudioChannel { LEFT, RIGHT, BOTH }

@Singleton
class HardwareTestDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    // ---- Vibration --------------------------------------------------------

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun hasVibrator(): Boolean = vibrator?.hasVibrator() == true

    /**
     * Whether the motor can vary intensity, not just switch on and off.
     * The query itself only exists from API 26.
     */
    fun hasAmplitudeControl(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            vibrator?.hasAmplitudeControl() == true

    /**
     * VibrationEffect arrived in API 26 and VibratorManager in API 31, so this
     * fans out across three eras of the API.
     */
    @Suppress("DEPRECATION")
    fun vibrate(durationMillis: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator?.vibrate(durationMillis)
            return
        }

        val effect = VibrationEffect.createOneShot(
            durationMillis,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.vibrate(CombinedVibration.createParallel(effect))
        } else {
            vibrator?.vibrate(effect)
        }
    }

    @Suppress("DEPRECATION")
    fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Pre-26 has no amplitude control at all — the pattern degrades to
            // on/off timings, which is the honest best this hardware era offers.
            vibrator?.vibrate(timings, -1)
            return
        }

        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.vibrate(CombinedVibration.createParallel(effect))
        } else {
            vibrator?.vibrate(effect)
        }
    }

    fun cancelVibration() {
        vibrator?.cancel()
    }

    // ---- Speaker ----------------------------------------------------------

    /**
     * Plays a sine tone through one or both channels so the user can tell the
     * speakers apart. Written as stereo PCM with the unused channel silenced,
     * because ToneGenerator cannot address a single channel.
     */
    suspend fun playTone(
        channel: AudioChannel,
        frequencyHz: Int = 440,
        durationMillis: Int = 1500
    ) = withContext(io) {
        val sampleRate = 44_100
        val frameCount = sampleRate * durationMillis / 1000
        // Interleaved stereo: [L, R, L, R, ...]
        val samples = ShortArray(frameCount * 2)

        for (i in 0 until frameCount) {
            // Fade the edges to avoid the click an abrupt start/stop produces.
            val fade = when {
                i < FADE_FRAMES -> i.toFloat() / FADE_FRAMES
                i > frameCount - FADE_FRAMES -> (frameCount - i).toFloat() / FADE_FRAMES
                else -> 1f
            }
            val value = (sin(2.0 * PI * frequencyHz * i / sampleRate) *
                Short.MAX_VALUE * 0.35 * fade).toInt().toShort()

            samples[i * 2] = if (channel != AudioChannel.RIGHT) value else 0
            samples[i * 2 + 1] = if (channel != AudioChannel.LEFT) value else 0
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(samples, 0, samples.size)
            track.play()
            // Hold the track alive for the duration, but stay cancellable.
            var elapsed = 0
            while (elapsed < durationMillis) {
                coroutineContext.ensureActive()
                kotlinx.coroutines.delay(50)
                elapsed += 50
            }
        } finally {
            runCatching { track.stop() }
            track.release()
        }
    }

    fun isMuted(): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }

    private companion object {
        const val FADE_FRAMES = 1000
    }
}
