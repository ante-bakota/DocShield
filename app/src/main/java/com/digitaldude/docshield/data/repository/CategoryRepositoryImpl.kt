package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.data.local.CategoryDao
import com.digitaldude.docshield.data.local.CategoryEntity
import com.digitaldude.docshield.domain.model.Category
import com.digitaldude.docshield.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return dao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addCategory(category: Category) {
        dao.insertCategory(category.toEntity())
    }

    override suspend fun deleteCategoryByName(name: String) {
        dao.deleteCategoryByName(name)
    }
}

private fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    createdAt = createdAt
)

private fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    createdAt = createdAt
)
