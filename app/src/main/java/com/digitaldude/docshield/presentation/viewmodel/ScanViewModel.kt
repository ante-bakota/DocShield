package com.digitaldude.docshield.presentation.viewmodel

import GeminiNanoDataSource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.ExtractTextUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val extractTextUseCase: ExtractTextUseCase,
    private val addDocumentUseCase: AddDocumentUseCase,
    private val geminiNanoDataSource: GeminiNanoDataSource
) : ViewModel() {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState : StateFlow<ScanState> = _scanState.asStateFlow()

    private val _aiSuggestedState = MutableStateFlow<String?>(null)
    val aiSuggestedState : StateFlow<String?> = _aiSuggestedState.asStateFlow()

    private val _aiSuggestedTitle = MutableStateFlow<String?>(null)
    val aiSuggestedTitle: StateFlow<String?> = _aiSuggestedTitle.asStateFlow()

    fun onDocumentScanned(imageUri: String){
        viewModelScope.launch {
            _scanState.value = ScanState.Loading
            val text = extractTextUseCase(imageUri)
            _scanState.value = ScanState.Success(imageUri, text)

        }
    }

    fun resetState(){
        _scanState.value = ScanState.Idle
    }

    fun saveDocument(title : String,extractedText: String, imageUri: String) {
        viewModelScope.launch {
            addDocumentUseCase(
                Document(
                    title = title.ifBlank { "Dokument bez naziva" },
                    extractedText = extractedText,
                    imageUri = imageUri,
                    category = "Ostalo"
                )
            )
            resetState()
        }
    }

    fun suggestTitleWithAi(extractedText: String) {
        viewModelScope.launch {
            val available = geminiNanoDataSource.isAvailable()
            if (available) {
                val suggestion = geminiNanoDataSource.suggestTitle(extractedText)
                _aiSuggestedTitle.value = suggestion
            } else {
                _aiSuggestedTitle.value = "Gemini Nano nije dostupan"
            }
        }
    }
}

sealed class ScanState{
    object Idle: ScanState()
    object Loading : ScanState()
    data class Success(val imageUri : String, val extractedText : String) : ScanState()
    data class Error(val message : String) : ScanState()
}