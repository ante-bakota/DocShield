package com.digitaldude.docshield.domain.model

data class ScannedDocument(
    val imageUri: String,
    val extractedText: String
)