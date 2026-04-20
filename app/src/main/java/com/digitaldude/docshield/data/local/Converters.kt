package com.digitaldude.docshield.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromImageUris(uris: List<String>): String = uris.joinToString("|")

    @TypeConverter
    fun toImageUris(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|")
}