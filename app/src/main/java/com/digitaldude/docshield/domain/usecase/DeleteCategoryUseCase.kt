package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String) = repository.deleteCategoryByName(name)
}
