package com.digitaldude.docshield.data.local

class ScanSessionHolder {
    private var pendingUris: List<String> = emptyList()

    fun set(uris: List<String>) {
        pendingUris = uris
    }

    // returns URIs and clears them so they can't be consumed twice
    fun consume(): List<String> {
        val uris = pendingUris
        pendingUris = emptyList()
        return uris
    }
}
