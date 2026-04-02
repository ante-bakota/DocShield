package com.digitaldude.docshield.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val title : String,
    val category : String,
    val extractedText : String,
    val imageUri : String,
    val dateAdded : Long = System.currentTimeMillis()
)