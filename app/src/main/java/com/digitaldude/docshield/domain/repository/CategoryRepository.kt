package com.digitaldude.docshield.domain.repository

import com.digitaldude.docshield.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategoryByName(name: String)
}
