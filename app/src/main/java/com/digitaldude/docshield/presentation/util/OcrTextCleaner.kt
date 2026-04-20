package com.digitaldude.docshield.presentation.util

fun cleanOcrText(raw: String): String {
    return raw
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && it.any { c -> c.isLetterOrDigit() } }
        .joinToString("\n")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
