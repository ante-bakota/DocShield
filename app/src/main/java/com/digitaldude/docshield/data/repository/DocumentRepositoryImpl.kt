package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.data.local.DocumentDao
import com.digitaldude.docshield.data.local.DocumentEntity
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(private val dao : DocumentDao) : DocumentRepository {
    override fun getDocuments(): Flow<List<Document>> {
        return dao.getAllDocuments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addDocument(document: Document) {
        dao.insertDocument(document.toEntity())
    }


}

private fun DocumentEntity.toDomain(): Document {
    return Document(
        id = id,
        title = title,
        category = category,
        extractedText = extractedText,
        imageUri = imageUri,
        dateAdded = dateAdded
    )
}

private fun Document.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        title = title,
        category = category,
        extractedText = extractedText,
        imageUri = imageUri,
        dateAdded = dateAdded
    )
}
