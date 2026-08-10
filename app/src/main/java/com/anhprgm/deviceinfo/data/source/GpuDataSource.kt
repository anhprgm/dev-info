package com.anhprgm.deviceinfo.data.source

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import com.anhprgm.deviceinfo.data.models.GpuInfo
import com.anhprgm.deviceinfo.data.models.VulkanInfo
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GPU identification.
 *
 * The renderer and vendor strings only exist inside a live GL context, so this
 * creates a 1x1 offscreen EGL pbuffer, queries, and tears it down. That is far
 * cheaper than instantiating a GLSurfaceView and needs no view hierarchy —
 * which matters because this runs from a singleton with no Activity.
 */
@Singleton
class GpuDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    /** GPU identity never changes; querying it costs a context creation. */
    @Volatile
    private var cached: GpuInfo? = null

    suspend fun getGpuInfo(): GpuInfo = withContext(io) {
        cached ?: buildGpuInfo().also { cached = it }
    }

    private fun buildGpuInfo(): GpuInfo {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // reqGlEsVersion is packed as major in the high 16 bits, minor in the low.
        val packed = activityManager.deviceConfigurationInfo.reqGlEsVersion
        val strings = queryGlStrings()

        return GpuInfo(
            renderer = strings?.renderer,
            vendor = strings?.vendor,
            glVersion = strings?.version,
            glesVersionMajor = (packed shr 16) and 0xFFFF,
            glesVersionMinor = packed and 0xFFFF,
            extensions = strings?.extensions.orEmpty(),
            vulkan = queryVulkan()
        )
    }

    private data class GlStrings(
        val renderer: String?,
        val vendor: String?,
        val version: String?,
        val extensions: List<String>
    )

    private fun queryGlStrings(): GlStrings? {
        var display: EGLDisplay? = null
        var surface: EGLSurface? = null
        var eglContext: EGLContext? = null

        return try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) return null

            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) return null

            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val configCount = IntArray(1)
            if (!EGL14.eglChooseConfig(
                    display, configAttribs, 0, configs, 0, 1, configCount, 0
                ) || configCount[0] == 0
            ) {
                return null
            }

            eglContext = EGL14.eglCreateContext(
                display,
                configs[0],
                EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE),
                0
            )
            if (eglContext == EGL14.EGL_NO_CONTEXT) return null

            // 1x1 is the smallest surface that still makes the context current.
            surface = EGL14.eglCreatePbufferSurface(
                display,
                configs[0],
                intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE),
                0
            )
            if (surface == EGL14.EGL_NO_SURFACE) return null
            if (!EGL14.eglMakeCurrent(display, surface, surface, eglContext)) return null

            GlStrings(
                renderer = GLES20.glGetString(GLES20.GL_RENDERER),
                vendor = GLES20.glGetString(GLES20.GL_VENDOR),
                version = GLES20.glGetString(GLES20.GL_VERSION),
                extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)
                    ?.split(' ')
                    ?.filter { it.isNotBlank() }
                    ?.sorted()
                    .orEmpty()
            )
        } catch (_: Throwable) {
            null
        } finally {
            if (display != null && display != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    display,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT
                )
                surface?.takeIf { it != EGL14.EGL_NO_SURFACE }
                    ?.let { EGL14.eglDestroySurface(display, it) }
                eglContext?.takeIf { it != EGL14.EGL_NO_CONTEXT }
                    ?.let { EGL14.eglDestroyContext(display, it) }
                EGL14.eglTerminate(display)
            }
        }
    }

    /**
     * FeatureInfo.version packs the Vulkan API version as
     * major(10 bits) | minor(10) | patch(12).
     */
    private fun queryVulkan(): VulkanInfo {
        val features = runCatching {
            context.packageManager.systemAvailableFeatures
        }.getOrNull().orEmpty()

        val hardwareVersion = features
            .firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }
        val hardwareLevel = features
            .firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL }

        if (hardwareVersion == null) {
            return VulkanInfo(false, null, null, null, null)
        }

        val packed = hardwareVersion.version
        return VulkanInfo(
            supported = true,
            apiVersionMajor = (packed ushr 22) and 0x3FF,
            apiVersionMinor = (packed ushr 12) and 0x3FF,
            apiVersionPatch = packed and 0xFFF,
            hardwareLevel = hardwareLevel?.version
        )
    }
}
