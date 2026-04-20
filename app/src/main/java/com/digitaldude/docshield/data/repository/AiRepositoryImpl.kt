package com.digitaldude.docshield.data.repository

import com.digitaldude.docshield.data.ml.GeminiNanoDataSource
import android.util.Log
import com.digitaldude.docshield.data.ml.LlmInferenceDataSource
import com.digitaldude.docshield.data.ml.RuleBasedCategorizerDataSource
import com.digitaldude.docshield.domain.model.AiSuggestion
import com.digitaldude.docshield.domain.repository.AiRepository

/**
 * Implementation of AiRepository that orchestrates a three-tier AI fallback chain.
 *
 * Tier 1 — LlmInferenceDataSource (MediaPipe): most flexible, supports free-form prompts,
 *           can provide both title and category. Currently unavailable (model not provisioned).
 *
 * Tier 2 — GeminiNanoDataSource (ML Kit): high quality title suggestion, device-limited
 *           (Pixel 9 / Galaxy S25+), requires 400+ chars. Provides title only —
 *           category always comes from RuleBased in this tier.
 *
 * Tier 3 — RuleBasedCategorizerDataSource: deterministic keyword matching, no AI,
 *           always works on every device.
 *
 * ViewModels and UseCases depend only on the AiRepository interface — they are
 * completely unaware of which tier is active at runtime.
 */

class AiRepositoryImpl(
    private val llmInferenceDataSource: LlmInferenceDataSource,
    private val geminiNano: GeminiNanoDataSource,
    private val ruleBased : RuleBasedCategorizerDataSource
) : AiRepository {


    companion object{
        private const val TAG = "AiRepositoryImpl"
        private val MIN_TEXT_LENGTH_FOR_GEMINI = 400
    }

    override suspend fun categorize(extractedText: String): AiSuggestion {
        //Tier1 - Media Pipe LLM inference (free form promp, best one here)
        if(llmInferenceDataSource.isAvailable()){
            Log.d(TAG, "Using Tier 1: LlmInference")
            val category = llmInferenceDataSource.categorize(extractedText)
            if (category != null) {
                return AiSuggestion(
                    suggestedCategory = category,
                    suggestedTitle = null // LlmInference title generation — future implementation
                )
            }
        }
        if (extractedText.length >= MIN_TEXT_LENGTH_FOR_GEMINI && geminiNano.isAvailable()){
            Log.d(TAG, "Using Tier 2: GeminiNano + RuleBased")
            val title = runCatching { geminiNano.suggestTitle(extractedText) }.getOrNull()
            val ruleBasedResult = ruleBased.categorize(extractedText)
            return AiSuggestion(
                suggestedCategory = ruleBasedResult.suggestedCategory,
                suggestedTitle = title ?: ruleBasedResult.suggestedTitle
            )
        }

        // Tier 3 — RuleBased (always works)
        Log.d(TAG, "Using Tier 3: RuleBased")
        return ruleBased.categorize(extractedText)
    }
}