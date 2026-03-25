package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.repository.DocumentRepository

class AddDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(document: Document) {
        repository.addDocument(document)
    }
}
