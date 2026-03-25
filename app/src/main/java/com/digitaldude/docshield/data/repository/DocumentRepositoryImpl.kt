package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DocumentRepositoryImpl : DocumentRepository {

    private var nextId = 1L
    private val _documents = MutableStateFlow<List<Document>>(emptyList())

    override fun getDocuments(): Flow<List<Document>> {
        return _documents.asStateFlow()
    }

    override suspend fun addDocument(document: Document) {
        val documentWithId = document.copy(id = nextId++)
        _documents.value = _documents.value + documentWithId
    }
}
