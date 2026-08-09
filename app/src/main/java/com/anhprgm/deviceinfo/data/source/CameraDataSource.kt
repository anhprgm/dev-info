package com.anhprgm.deviceinfo.data.source

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.anhprgm.deviceinfo.data.models.CameraDetail
import com.anhprgm.deviceinfo.data.models.CameraInfo
import com.anhprgm.deviceinfo.data.models.LensFacing
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads camera capabilities. Note this needs no CAMERA permission — querying
 * [CameraCharacteristics] is metadata access, not capture. The manifest used to
 * declare CAMERA and never request it at runtime.
 */
@Singleton
class CameraDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun getCameraInfo(): CameraInfo = withContext(io) {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val ids = try {
            manager.cameraIdList
        } catch (_: Exception) {
            emptyArray()
        }

        val cameras = ids.mapNotNull { id -> readCamera(manager, id) }
        CameraInfo(cameraCount = cameras.size, cameras = cameras)
    }

    private fun readCamera(manager: CameraManager, id: String): CameraDetail? = try {
        val c = manager.getCameraCharacteristics(id)
        val pixelArray = c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)

        CameraDetail(
            cameraId = id,
            facing = when (c.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_FRONT -> LensFacing.FRONT
                CameraCharacteristics.LENS_FACING_BACK -> LensFacing.BACK
                CameraCharacteristics.LENS_FACING_EXTERNAL -> LensFacing.EXTERNAL
                else -> LensFacing.UNKNOWN
            },
            flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false,
            sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0,
            pixelWidth = pixelArray?.width,
            pixelHeight = pixelArray?.height,
            focalLengthsMm = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?.toList().orEmpty(),
            aperturesFStop = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                ?.toList().orEmpty(),
            opticalStabilization = c
                .get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                ?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) ?: false,
            autoExposureLock = c.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE) ?: false,
            autoWhiteBalanceLock = c.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE) ?: false,
            outputFormats = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.outputFormats
                ?.map(::formatName)
                ?.distinct()
                .orEmpty()
        )
    } catch (_: Exception) {
        null
    }

    private fun formatName(format: Int): String = when (format) {
        ImageFormat.JPEG -> "JPEG"
        ImageFormat.RAW_SENSOR -> "RAW"
        ImageFormat.RAW_PRIVATE -> "RAW_PRIVATE"
        ImageFormat.YUV_420_888 -> "YUV_420_888"
        ImageFormat.YUV_422_888 -> "YUV_422_888"
        ImageFormat.PRIVATE -> "PRIVATE"
        ImageFormat.HEIC -> "HEIC"
        ImageFormat.DEPTH16 -> "DEPTH16"
        ImageFormat.FLEX_RGB_888 -> "RGB_888"
        else -> "0x${Integer.toHexString(format)}"
    }
}
