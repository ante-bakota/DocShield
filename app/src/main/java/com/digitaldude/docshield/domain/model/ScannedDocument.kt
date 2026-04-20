package com.digitaldude.docshield.domain.model

data class ScannedDocument(
    val imageUris: List<String>,
    val extractedText: String
)