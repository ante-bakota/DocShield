package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.data.ml.TextRecognitionDataSource
import com.digitaldude.docshield.domain.repository.ScanRepository

class ScanRepositoryImpl(private val textRecognitionDataSource: TextRecognitionDataSource) : ScanRepository{
    override suspend fun extractText(imageUris: List<String>): String {
        return imageUris
            .map { textRecognitionDataSource.extractText(it) }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }
}