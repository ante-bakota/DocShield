package com.digitaldude.docshield.data.local

import androidx.room.TypeConverter
import com.digitaldude.docshield.domain.model.DocumentType

class Converters {
    @TypeConverter
    fun fromImageUris(uris: List<String>): String = uris.joinToString("|")

    @TypeConverter
    fun toImageUris(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|")

    @TypeConverter
    fun fromDocumentType(type: DocumentType) : String = type.name

    @TypeConverter
    fun toDocumentType(value: String) : DocumentType = DocumentType.valueOf(value)
}