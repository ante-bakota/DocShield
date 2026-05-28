package com.digitaldude.docshield.data.ml

import com.digitaldude.docshield.domain.model.AiSuggestion

class RuleBasedCategorizerDataSource{

    //TODO implemented as fallback when media pipe fails
    //Will implement media pipe soon

    private val categoryKeywords : Map<String, List<String>> = mapOf(
        "Invoice" to listOf(
            "račun", "faktura", "iznos", "ukupno", "pdv", "plaćanje",
            "invoice", "total", "amount", "payment", "due", "bill",
            "ugovor", "suglasnost", "ugovorne strane", "odredbe",
            "contract", "agreement", "terms", "conditions",
            "garancija", "jamstvo", "serijski broj",
            "warranty", "guarantee", "serial number"
        ),
        "Health" to listOf(
            "dijagnoza", "pacijent", "liječnik", "recept", "nalaz", "terapija",
            "diagnosis", "patient", "doctor", "prescription", "medical"
        ),
        "Identity" to listOf(
            "osobna iskaznica", "putovnica", "vozačka dozvola", "oib",
            "passport", "id card", "driving license"
        )
    )

    fun categorize(extractedText: String): AiSuggestion{
        val category = detectCategory(extractedText)
        val title = generateBasicTitle(extractedText, category)
        return AiSuggestion(suggestedCategory = category, suggestedTitle = title)
    }

    private fun detectCategory(text: String): String {
        val lowerText = text.lowercase()

        val scores: Map<String, Int> = categoryKeywords.mapValues { (_, keywords) ->
            keywords.count { keyword -> lowerText.contains(keyword) }
        }

        val bestMatch = scores.maxByOrNull { it.value }
        return if (bestMatch != null && bestMatch.value > 0) bestMatch.key else "Other"
    }

    private fun generateBasicTitle(text: String, category: String): String? {
        val firstMeaningfulLine = text.lines()
            .map { it.trim() }
            .firstOrNull { it.length in 3..60 }

        return firstMeaningfulLine?.let { "$category — $it" }
    }
}