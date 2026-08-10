package com.anhprgm.deviceinfo.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.log10

/**
 * Microphone level meter.
 *
 * Audio is read into a short-lived in-memory buffer purely to compute an RMS
 * level and is never written to disk, uploaded, or retained — the test only
 * needs "is the mic picking anything up".
 */
@Singleton
class MicrophoneDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun hasMicrophone(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

    /**
     * Emits a 0..1 normalised level. Requires RECORD_AUDIO; the flow completes
     * immediately without it rather than throwing.
     */
    @SuppressLint("MissingPermission")
    fun levels(): Flow<Float> = flow {
        if (!hasPermission()) return@flow

        val sampleRate = 44_100
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return@flow

        val bufferSize = minBuffer * 2
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return@flow
        }

        val buffer = ShortArray(bufferSize / 2)
        try {
            record.startRecording()
            while (currentCoroutineContext().isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    emit(normalisedLevel(buffer, read))
                }
            }
        } finally {
            runCatching { record.stop() }
            record.release()
        }
    }.flowOn(io)

    /** Peak amplitude mapped through dBFS onto 0..1 so quiet speech is visible. */
    private fun normalisedLevel(buffer: ShortArray, length: Int): Float {
        var peak = 0
        for (i in 0 until length) {
            val magnitude = abs(buffer[i].toInt())
            if (magnitude > peak) peak = magnitude
        }
        if (peak == 0) return 0f

        val dbfs = 20 * log10(peak.toDouble() / Short.MAX_VALUE)
        // -60 dBFS is effectively silence for this purpose.
        return ((dbfs + 60) / 60).coerceIn(0.0, 1.0).toFloat()
    }
}
