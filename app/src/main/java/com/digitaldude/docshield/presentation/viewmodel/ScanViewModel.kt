package com.digitaldude.docshield.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.CategorizeDocumentUseCase
import com.digitaldude.docshield.domain.usecase.ExtractTextUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val extractTextUseCase: ExtractTextUseCase,
    private val addDocumentUseCase: AddDocumentUseCase,
    private val categorizeDocumentUseCase: CategorizeDocumentUseCase
) : ViewModel() {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState : StateFlow<ScanState> = _scanState.asStateFlow()

    private val _aiSuggestedTitle = MutableStateFlow<String?>(null)
    val aiSuggestedTitle: StateFlow<String?> = _aiSuggestedTitle.asStateFlow()

    private val _aiSuggestedCategory = MutableStateFlow<String?>(null)
    val aiSuggestedCategory: StateFlow<String?> = _aiSuggestedCategory.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun onDocumentScanned(imageUris: List<String>){
        viewModelScope.launch {
            _scanState.value = ScanState.Loading
            val text = extractTextUseCase(imageUris)
            _scanState.value = ScanState.Success(imageUris, text)
        }
    }

    fun resetState(){
        _scanState.value = ScanState.Idle
    }

    /**
     * Runs AI analysis on the scanned text — populates both title and category suggestions.
     * Uses the full fallback chain via CategorizeDocumentUseCase, so the ViewModel
     * has no knowledge of which AI tier is active.
     */
    fun suggestWithAi(extractedText: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val suggestion = categorizeDocumentUseCase(extractedText)
            _aiSuggestedTitle.value = suggestion.suggestedTitle
            _aiSuggestedCategory.value = suggestion.suggestedCategory
            _isAiLoading.value = false
        }
    }

    fun saveDocument(title: String, extractedText: String, imageUris: List<String>, category: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            addDocumentUseCase(
                Document(
                    title = title.ifBlank { "Dokument bez naziva" },
                    extractedText = extractedText,
                    imageUris = imageUris,
                    category = category
                )
            )
            resetState()
            onSaved()
        }
    }
}

sealed class ScanState{
    object Idle: ScanState()
    object Loading : ScanState()
    data class Success(val imageUris : List<String>, val extractedText : String) : ScanState()
    data class Error(val message : String) : ScanState()
}