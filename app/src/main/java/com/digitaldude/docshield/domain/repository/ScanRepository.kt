package com.digitaldude.docshield.domain.repository

interface ScanRepository {
    suspend fun extractText(imageUri: String): String
}