package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class GetDocumentsUseCase(private val repository: DocumentRepository) {
    operator fun invoke(): Flow<List<Document>> {
        return repository.getDocuments()
    }
}
