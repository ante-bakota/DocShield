package com.digitaldude.docshield.data.ml
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.ai.edge.litertlm.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Renders each PDF page to Bitmap and extracts text via ML Kit OCR.
// PdfRenderer is Android's built-in PDF engine — no external library needed.
class PdfTextExtractionDataSource(
    private val context: Context,
    private val textRecognitionDataSource: TextRecognitionDataSource
) {
    suspend fun extractText(pdfUri: Uri): String = withContext(Dispatchers.IO) {
        val fileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: return@withContext ""

        val renderer = PdfRenderer(fileDescriptor)
        val pageTexts = mutableListOf<String>()

        for (pageIndex in 0 until renderer.pageCount) {
            val page = renderer.openPage(pageIndex)

            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // Switch to Default (Main) dispatcher for ML Kit which requires it
            val pageText = withContext(Dispatchers.Default) {
                textRecognitionDataSource.extractTextFromBitmap(bitmap)
            }
            pageTexts.add(pageText)
            bitmap.recycle()
        }

        renderer.close()
        fileDescriptor.close()

        pageTexts.joinToString("\n\n")
    }
}