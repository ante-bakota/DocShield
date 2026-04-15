package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.model.AiSuggestion
import com.digitaldude.docshield.domain.repository.AiRepository

// This class triggers AI categorization for a given OCR text

class CategorizeDocumentUseCase(private val aiRepository: AiRepository) {
    suspend operator fun invoke(extractedText : String) : AiSuggestion {
        return  aiRepository.categorize(extractedText)
    }
}