package com.digitaldude.docshield.domain.model

/**
 * Represents results of an Ai categorization request
 * * [suggestedCategory] is always present — worst case it will be "Other".
 *    * [suggestedTitle] is optional — rule-based fallback may not produce one.
 */

data class AiSuggestion(
    val suggestedCategory : String,
    val suggestedTitle : String?
)