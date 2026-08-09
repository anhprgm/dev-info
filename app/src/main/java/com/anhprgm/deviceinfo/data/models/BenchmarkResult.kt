package com.anhprgm.deviceinfo.data.models

/**
 * Raw timings are the source of truth; scores are derived.
 *
 * The previous scoring divided a hard-coded baseline by the elapsed time and
 * clamped to 1000, so every reasonably fast device saturated at exactly 1000
 * and the numbers could not be compared. Keeping the durations lets Phase 7
 * re-derive scores without invalidating stored results.
 */
data class BenchmarkResult(
    val singleCoreMillis: Long,
    val multiCoreMillis: Long,
    val memoryMillis: Long,
    val coresUsed: Int,
    val totalDurationMillis: Long,
    val timestamp: Long
) {
    val singleCoreScore: Int get() = scoreOf(singleCoreMillis, SINGLE_CORE_BASELINE_MS)
    val multiCoreScore: Int get() = scoreOf(multiCoreMillis, MULTI_CORE_BASELINE_MS)
    val memoryScore: Int get() = scoreOf(memoryMillis, MEMORY_BASELINE_MS)
    val cpuScore: Int get() = (singleCoreScore + multiCoreScore) / 2
    val overallScore: Int get() = (cpuScore + memoryScore) / 2

    private companion object {
        const val SINGLE_CORE_BASELINE_MS = 1000.0
        const val MULTI_CORE_BASELINE_MS = 300.0
        const val MEMORY_BASELINE_MS = 150.0

        fun scoreOf(actualMs: Long, baselineMs: Double): Int {
            if (actualMs <= 0) return 0
            return (baselineMs / actualMs * 1000.0).coerceIn(0.0, 1000.0).toInt()
        }
    }
}
