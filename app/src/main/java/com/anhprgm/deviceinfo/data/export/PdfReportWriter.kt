package com.anhprgm.deviceinfo.data.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.anhprgm.deviceinfo.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes the report as a PDF using the framework's own PdfDocument.
 *
 * No PDF library is pulled in on purpose: iText is AGPL or commercial and
 * PDFBox-Android adds roughly 10 MB to an app whose whole release APK is under
 * 2 MB. The default typeface also renders Vietnamese diacritics correctly,
 * which a bundled PDF font would not without shipping a full Unicode face.
 */
@Singleton
class PdfReportWriter @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend fun write(text: String, target: File): File = withContext(io) {
        val document = PdfDocument()
        val bodyPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = BODY_SIZE
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
        val headingPaint = TextPaint().apply {
            color = HEADING_COLOR
            textSize = HEADING_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var pageNumber = 1
        var page = startPage(document, pageNumber)
        var canvas = page.canvas
        var y = MARGIN + BODY_SIZE

        text.lineSequence().forEach { line ->
            // Manual pagination: PdfDocument has no concept of flowing text.
            if (y > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                pageNumber++
                page = startPage(document, pageNumber)
                canvas = page.canvas
                y = MARGIN + BODY_SIZE
            }

            // A dashed rule in the plain-text report marks a section title.
            val isUnderline = line.isNotEmpty() && line.all { it == '-' }
            if (isUnderline) {
                canvas.drawLine(MARGIN, y - BODY_SIZE / 2, PAGE_WIDTH - MARGIN, y - BODY_SIZE / 2, rulePaint)
                y += LINE_HEIGHT
                return@forEach
            }

            val isHeading = line.isNotBlank() && line == line.uppercase() && !line.contains(':')
            canvas.drawText(
                truncateToWidth(line, if (isHeading) headingPaint else bodyPaint),
                MARGIN,
                y,
                if (isHeading) headingPaint else bodyPaint
            )
            y += if (isHeading) LINE_HEIGHT + 4f else LINE_HEIGHT
        }

        document.finishPage(page)

        try {
            FileOutputStream(target).use { document.writeTo(it) }
        } finally {
            document.close()
        }
        target
    }

    private fun startPage(document: PdfDocument, number: Int): PdfDocument.Page =
        document.startPage(
            PdfDocument.PageInfo.Builder(
                PAGE_WIDTH.toInt(),
                PAGE_HEIGHT.toInt(),
                number
            ).create()
        )

    /**
     * Build fingerprints run well past the page width, so clip rather than let
     * the text run off the paper.
     */
    private fun truncateToWidth(line: String, paint: Paint): String {
        val maxWidth = PAGE_WIDTH - MARGIN * 2
        if (paint.measureText(line) <= maxWidth) return line

        var end = line.length
        while (end > 0 && paint.measureText(line.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return line.substring(0, end) + "…"
    }

    private val rulePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
    }

    private companion object {
        // A4 at 72 dpi, the unit PdfDocument works in.
        const val PAGE_WIDTH = 595f
        const val PAGE_HEIGHT = 842f
        const val MARGIN = 40f
        const val BODY_SIZE = 9f
        const val HEADING_SIZE = 12f
        const val LINE_HEIGHT = 12f
        const val HEADING_COLOR = 0xFF3F51B5.toInt()
    }
}
