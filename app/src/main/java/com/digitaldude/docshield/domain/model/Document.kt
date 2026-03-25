package com.digitaldude.docshield.domain.model

data class Document(
    val id: Long = 0,
    val title :  String,
    val category: String,
    val dateAdded : Long = System.currentTimeMillis()
)