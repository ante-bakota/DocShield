package com.digitaldude.docshield.domain.repository

import com.digitaldude.docshield.domain.model.AiSuggestion

/**
 *
 * Contract for on device AI document categorization
 *
 * DocShield uses 3 tier fallback chain to ensure the feature works regardless of hardware capabilities
 *
 * 1. Gemini Nano -  on device LLM (Pixel 8+ and newer phones)
 * 2. MediaPipe LLM interfence - local model file,works on most mid range devices
 * 3. Rule based categorizer - keyword mathching , zero dependencies, always avaliable
 *
 *This interface lives in the domain layer and has NO knowledge of which
 *    * implementation is active. The concrete strategy is decided in AiRepositoryImpl
 *    * at runtime, keeping this contract clean and testable.
 *    *
 *    * All implementations must be safe to call from a background coroutine —
 *    * LLM inference can take several seconds on slower devices.
 */

interface AiRepository {
    // Analyzes the OCR-extracted text and returns a category and optional title suggestion.

    suspend fun categorize(extractedText : String) : AiSuggestion
}