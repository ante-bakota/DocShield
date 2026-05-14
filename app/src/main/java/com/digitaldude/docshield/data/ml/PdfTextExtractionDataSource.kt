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
            // PdfRenderer doesnt fill the background — transparent pixels become black when saved as JPEG
            bitmap.eraseColor(android.graphics.Color.WHITE)
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

    // Renders each PDF page to a JPEG file in filesDir and returns their URIs.
    // Called once at import time — files persist so DetailScreen never re-renders.
    suspend fun renderPagesToFiles(pdfUri: Uri, filePrefix: String): List<Uri> = withContext(Dispatchers.IO) {
        val fileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: return@withContext emptyList()

        val renderer = PdfRenderer(fileDescriptor)
        val imageUris = mutableListOf<Uri>()

        for (pageIndex in 0 until renderer.pageCount) {
            val page = renderer.openPage(pageIndex)

            // Scale up for readable resolution — raw PDF units are small
            val scale = 2
            val bitmap = Bitmap.createBitmap(
                page.width * scale,
                page.height * scale,
                Bitmap.Config.ARGB_8888
            )
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val file = java.io.File(context.filesDir, "${filePrefix}_page_$pageIndex.jpg")
            file.outputStream().use { out ->
                // JPEG quality 90 — good balance between file size and readability
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()

            imageUris.add(Uri.fromFile(file))
        }

        renderer.close()
        fileDescriptor.close()

        imageUris
    }
}