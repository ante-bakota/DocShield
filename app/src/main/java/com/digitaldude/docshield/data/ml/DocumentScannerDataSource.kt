package com.digitaldude.docshield.data.ml

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

class DocumentScannerDataSource(private val activity: ComponentActivity) {

    private var onResult: ((List<String>) -> Unit)? = null

    private val scanner = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(10)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions. SCANNER_MODE_FULL)
            .build()
    )

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val uris = scanResult?.pages?.map { it.imageUri.toString() } ?: emptyList()
            onResult?.invoke(uris)
        } else {
            onResult?.invoke(emptyList())
        }
    }

    fun startScan(onBeforeLaunch: () -> Unit = {}, onResult: (List<String?>) -> Unit) {
        this.onResult = onResult
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                onBeforeLaunch()  // set flag right before the external Activity launches
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "Scanner error: ${exception.message}", Toast.LENGTH_LONG).show()
                onResult(emptyList())
            }
    }
}