package com.digitaldude.docshield.domain.usecase

import com.digitaldude.docshield.domain.repository.ScanRepository

class ExtractTextUseCase(private val repository : ScanRepository) {
    suspend operator fun invoke(imageUri : String)  : String {
        return repository.extractText(imageUri)
    }
}