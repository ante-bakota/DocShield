package com.digitaldude.docshield.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.digitaldude.docshield.domain.model.DocumentType

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val title : String,
    val category : String,
    val extractedText : String,
    val imageUris : List<String>,
    val dateAdded : Long = System.currentTimeMillis(),
    val documentType: String = DocumentType.SCANNED.name
)