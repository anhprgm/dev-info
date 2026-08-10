package com.anhprgm.deviceinfo.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.anhprgm.deviceinfo.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class ExportFormat(val extension: String, val mimeType: String) {
    TEXT("txt", "text/plain"),
    JSON("json", "application/json"),
    PDF("pdf", "application/pdf")
}

@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serializer: ReportSerializer,
    private val pdfWriter: PdfReportWriter,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    /**
     * Writes into cacheDir rather than external storage: FileProvider can grant
     * a temporary read URI from there, so sharing needs no storage permission
     * and the OS reclaims the file on its own.
     */
    private val reportsDir: File
        get() = File(context.cacheDir, "reports").apply { mkdirs() }

    suspend fun export(report: DeviceReport, format: ExportFormat): File = withContext(io) {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT)
            .format(Date(report.generatedAtMillis))
        val safeModel = report.device.model
            .replace(Regex("[^A-Za-z0-9._-]"), "-")
            .take(32)
        val file = File(reportsDir, "devinfo-$safeModel-$stamp.${format.extension}")

        when (format) {
            ExportFormat.TEXT -> file.writeText(serializer.toPlainText(report))
            ExportFormat.JSON -> file.writeText(serializer.toJson(report))
            ExportFormat.PDF -> pdfWriter.write(serializer.toPlainText(report), file)
        }
        file
    }

    fun uriFor(file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    fun shareIntent(file: File, format: ExportFormat): Intent {
        val uri = uriFor(file)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                // Without this grant the receiving app cannot open the file.
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            null
        ).apply { addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    fun shareImageIntent(file: File): Intent {
        val uri = uriFor(file)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            null
        ).apply { addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    suspend fun writeImage(bytes: ByteArray, name: String): File = withContext(io) {
        File(reportsDir, name).apply { writeBytes(bytes) }
    }

    /** Old exports pile up in the cache; drop anything past a day. */
    suspend fun pruneOldReports() = withContext(io) {
        val cutoff = System.currentTimeMillis() - MAX_AGE_MILLIS
        reportsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) file.delete()
        }
        Unit
    }

    private companion object {
        const val MAX_AGE_MILLIS = 24L * 60 * 60 * 1000
    }
}
