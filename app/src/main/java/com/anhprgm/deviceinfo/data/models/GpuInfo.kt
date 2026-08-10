package com.anhprgm.deviceinfo.data.models

data class GpuInfo(
    /** From glGetString(GL_RENDERER); needs a live GL context to obtain. */
    val renderer: String?,
    val vendor: String?,
    val glVersion: String?,
    /** Major.minor advertised by ActivityManager, available with no GL context. */
    val glesVersionMajor: Int,
    val glesVersionMinor: Int,
    val extensions: List<String>,
    val vulkan: VulkanInfo
) {
    val glesVersion: String get() = "$glesVersionMajor.$glesVersionMinor"
}

/**
 * Vulkan support is reported via PackageManager feature flags.
 *
 * The physical-device name and driver version are NOT obtainable without the
 * NDK (vkEnumeratePhysicalDevices), so we report support level and API version
 * and stop there rather than adding a C++ toolchain for one string.
 */
data class VulkanInfo(
    val supported: Boolean,
    val apiVersionMajor: Int?,
    val apiVersionMinor: Int?,
    val apiVersionPatch: Int?,
    /** 0 = baseline, 1 = adds more features. Null when unsupported. */
    val hardwareLevel: Int?
) {
    val apiVersion: String?
        get() {
            val major = apiVersionMajor ?: return null
            return "$major.${apiVersionMinor ?: 0}.${apiVersionPatch ?: 0}"
        }
}
