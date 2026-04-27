package com.digitaldude.docshield.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TextRecognitionDataSource(private val context : Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


    suspend fun extractText(imageUri : String) : String{
        val uri = Uri.parse(imageUri)
        val inputImage = InputImage.fromFilePath(context, uri)

        return suspendCoroutine {
            continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { continuation.resume("") }
        }
    }

    suspend fun extractTextFromBitmap(bitmap: Bitmap) : String{
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return suspendCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { continuation.resume("") }
        }
    }
}