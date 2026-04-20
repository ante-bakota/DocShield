package com.digitaldude.docshield.domain.repository

interface ScanRepository {
    suspend fun extractText(imageUris: List<String>): String
}