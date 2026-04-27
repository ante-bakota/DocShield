package com.digitaldude.docshield.domain.model

enum class DocumentType { SCANNED, PDF }

data class Document(
    val id: Long = 0,
    val title: String,
    val category: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val extractedText: String = "",
    val imageUris: List<String> = emptyList(),
    val documentType: DocumentType = DocumentType.SCANNED
)

