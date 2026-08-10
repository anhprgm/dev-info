package com.anhprgm.deviceinfo.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 *
 * Replaces the 14 magic strings in MainActivity. The concrete win beyond
 * tidiness is [AppDetail]: the old code stashed the selected app in the shared
 * ViewModel and read it back after navigating, so a deep link or a process
 * death on that screen rendered nothing at all. Carrying the package name in
 * the route lets the destination re-resolve its own data.
 */

// ---- Tab graphs -----------------------------------------------------------
@Serializable data object InfoGraph
@Serializable data object MonitorGraph
@Serializable data object TestGraph
@Serializable data object ToolsGraph

// ---- Info tab -------------------------------------------------------------
@Serializable data object Dashboard
@Serializable data object DeviceDetail
@Serializable data object Hardware
@Serializable data object Storage
@Serializable data object Battery
@Serializable data object Display
@Serializable data object Network
@Serializable data object Camera
@Serializable data object Sensors
@Serializable data object Gpu
@Serializable data object Sim
@Serializable data object Codecs
@Serializable data object Security

// ---- Monitor tab ----------------------------------------------------------
@Serializable data object Monitoring
@Serializable data object History
@Serializable data object Benchmark
@Serializable data object Thermal

// ---- Test tab -------------------------------------------------------------
@Serializable data object TestHub
@Serializable data class SensorLive(val sensorType: Int)
@Serializable data object SpeakerTest
@Serializable data object MicrophoneTest
@Serializable data object VibrationTest
@Serializable data object ButtonTest

/**
 * Immersive destinations. Declared outside the tab scaffold so the navigation
 * bar does not cover the area under test — a nav bar defeats a dead-pixel scan.
 */
@Serializable data object ScreenTest
@Serializable data object MultiTouchTest

// ---- Tools tab ------------------------------------------------------------
@Serializable data object ToolsHub
@Serializable data object AppList
@Serializable data class AppDetail(val packageName: String)
@Serializable data object ExportReport
@Serializable data object DeviceCard
@Serializable data object Compare
@Serializable data object Settings
