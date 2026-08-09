package com.anhprgm.deviceinfo.data.benchmark

import com.anhprgm.deviceinfo.data.models.BenchmarkResult
import com.anhprgm.deviceinfo.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * Deliberately simple CPU/memory workload. Results are only meaningful
 * relative to other runs on the same build — see [BenchmarkResult] for why the
 * derived scores saturate.
 */
@Singleton
class BenchmarkRunner @Inject constructor(
    @DefaultDispatcher private val default: CoroutineDispatcher
) {
    suspend fun run(): BenchmarkResult = withContext(default) {
        val cores = Runtime.getRuntime().availableProcessors()

        var singleCore = 0L
        var multiCore = 0L
        var memory = 0L

        val total = measureTimeMillis {
            singleCore = measureTimeMillis { arithmeticLoad(SINGLE_CORE_ITERATIONS) }
            multiCore = measureTimeMillis { runParallel(cores, MULTI_CORE_ITERATIONS) }
            memory = measureTimeMillis { memoryLoad() }
        }

        BenchmarkResult(
            singleCoreMillis = singleCore,
            multiCoreMillis = multiCore,
            memoryMillis = memory,
            coresUsed = cores,
            totalDurationMillis = total,
            timestamp = System.currentTimeMillis()
        )
    }

    /** Coroutines on the Default dispatcher rather than raw Threads. */
    private suspend fun runParallel(cores: Int, iterations: Int) = coroutineScope {
        (0 until cores)
            .map { async { arithmeticLoad(iterations) } }
            .awaitAll()
    }

    private fun arithmeticLoad(iterations: Int): Double {
        var result = 0.0
        for (i in 0 until iterations) {
            result += sqrt(i.toDouble())
            result += i.toDouble().pow(0.5)
        }
        return result
    }

    private fun memoryLoad(): Long {
        val array = IntArray(ARRAY_SIZE) { it }
        var sum = 0L
        repeat(MEMORY_PASSES) {
            for (i in 0 until ARRAY_SIZE) {
                sum += array[i]
            }
        }
        return sum
    }

    private companion object {
        const val SINGLE_CORE_ITERATIONS = 2_000_000
        const val MULTI_CORE_ITERATIONS = 1_000_000
        const val ARRAY_SIZE = 1_000_000
        const val MEMORY_PASSES = 5
    }
}
