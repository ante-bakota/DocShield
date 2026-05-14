package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.data.ml.TextRecognitionDataSource
import com.digitaldude.docshield.domain.repository.ScanRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ScanRepositoryImpl(private val textRecognitionDataSource: TextRecognitionDataSource) : ScanRepository{
    override suspend fun extractText(imageUris: List<String>): String {
        return coroutineScope {
            imageUris
                .map { uri -> async {textRecognitionDataSource.extractText(uri) } }
                .awaitAll()

        }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }
}