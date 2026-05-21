package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.model.Category
import com.digitaldude.docshield.domain.repository.CategoryRepository

class AddCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.addCategory(category)
}
