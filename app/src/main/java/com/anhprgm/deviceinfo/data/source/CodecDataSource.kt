package com.anhprgm.deviceinfo.data.source

import android.media.MediaCodecList
import android.os.Build
import com.anhprgm.deviceinfo.data.models.CodecInfo
import com.anhprgm.deviceinfo.data.models.MediaCodecDetail
import com.anhprgm.deviceinfo.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodecDataSource @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher
) {
    /** The codec list is fixed for the OS image, so enumerate it once. */
    @Volatile
    private var cached: CodecInfo? = null

    suspend fun getCodecInfo(): CodecInfo = withContext(io) {
        cached ?: build().also { cached = it }
    }

    private fun build(): CodecInfo {
        val infos = runCatching {
            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.toList()
        }.getOrNull().orEmpty()

        val details = infos.mapNotNull { info ->
            runCatching {
                MediaCodecDetail(
                    name = info.name,
                    mimeTypes = info.supportedTypes.toList().sorted(),
                    // These predicates only exist from API 29; below that the
                    // convention was a name prefix, which is unreliable.
                    isHardwareAccelerated =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            info.isHardwareAccelerated,
                    isSoftwareOnly = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        info.isSoftwareOnly,
                    isEncoder = info.isEncoder
                )
            }.getOrNull()
        }

        return CodecInfo(
            decoders = details.filterNot { it.isEncoder }.sortedBy { it.name },
            encoders = details.filter { it.isEncoder }.sortedBy { it.name }
        )
    }
}
