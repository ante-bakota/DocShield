package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.model.Category
import com.digitaldude.docshield.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repository.getCategories()
}
