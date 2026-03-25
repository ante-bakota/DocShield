package com.digitaldude.docshield.domain.repository

import com.digitaldude.docshield.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository  {
    fun getDocuments(): Flow<List<Document>>
    suspend fun addDocument(document: Document)
}
