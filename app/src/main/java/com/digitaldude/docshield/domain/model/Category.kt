package com.digitaldude.docshield.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
