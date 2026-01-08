package com.anhprgm.deviceinfo.data.models

data class BenchmarkResult(
    val cpuScore: Int,
    val singleCoreScore: Int,
    val multiCoreScore: Int,
    val memoryScore: Int,
    val overallScore: Int,
    val duration: Long
)
