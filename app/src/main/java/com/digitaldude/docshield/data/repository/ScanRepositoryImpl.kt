package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.data.ml.TextRecognitionDataSource
import com.digitaldude.docshield.domain.repository.ScanRepository

class ScanRepositoryImpl(private val textRecognitionDataSource: TextRecognitionDataSource) : ScanRepository{
    override suspend fun extractText(imageUri: String): String {
        return textRecognitionDataSource.extractText(imageUri)
    }
}