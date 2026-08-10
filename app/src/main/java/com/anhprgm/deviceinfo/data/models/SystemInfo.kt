package com.anhprgm.deviceinfo.data.models

/** Thermal readings. Almost everything here is API- or device-gated. */
data class ThermalInfo(
    val batteryTemperatureCelsius: Float?,
    /** PowerManager thermal status, API 29+. */
    val thermalStatus: ThermalStatus?,
    /**
     * API 30+; returns NaN on devices with no thermal HAL, which we map to
     * null rather than plotting a bogus 0.
     */
    val thermalHeadroom: Float?,
    val requiresApiForStatus: Int?,
    val requiresApiForHeadroom: Int?
)

enum class ThermalStatus { NONE, LIGHT, MODERATE, SEVERE, CRITICAL, EMERGENCY, SHUTDOWN, UNKNOWN }

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    /** Sum of all visible APK sizes; needs no permission. */
    val appsBytes: Long?,
    val installedAppCount: Int,
    val externalTotalBytes: Long?,
    val externalAvailableBytes: Long?,
    val isEmulatedExternal: Boolean
) {
    val usedBytes: Long get() = totalBytes - availableBytes
    val usagePercent: Float
        get() = if (totalBytes > 0) usedBytes * 100f / totalBytes else 0f

    /** Everything not accounted for by app APKs — system image, user data, cache. */
    val systemAndDataBytes: Long?
        get() = appsBytes?.let { (usedBytes - it).coerceAtLeast(0L) }
}

data class CodecInfo(
    val decoders: List<MediaCodecDetail>,
    val encoders: List<MediaCodecDetail>
)

data class MediaCodecDetail(
    val name: String,
    val mimeTypes: List<String>,
    val isHardwareAccelerated: Boolean,
    val isSoftwareOnly: Boolean,
    val isEncoder: Boolean
)

/**
 * Locally observable integrity signals.
 *
 * Deliberately NOT called "security status": every root check here is a
 * heuristic, and false positives on custom ROMs are what generate one-star
 * reviews. Play Integrity is not included because its client token is opaque
 * and only meaningful after server-side decoding, which needs a backend.
 */
data class SecurityInfo(
    val encryptionStatus: EncryptionStatus,
    val bootloader: String,
    val verifiedBootState: String?,
    val bootloaderLocked: Boolean?,
    val selinuxMode: String?,
    val rootIndicators: List<String>,
    val hasTestKeys: Boolean,
    val isDebuggableBuild: Boolean,
    val securityPatch: String?
) {
    /** True only when something was actually observed — never asserted as certainty. */
    val rootLikely: Boolean get() = rootIndicators.isNotEmpty()
}

enum class EncryptionStatus { UNSUPPORTED, INACTIVE, ACTIVE, ACTIVE_DEFAULT_KEY, ACTIVE_PER_USER, UNKNOWN }
