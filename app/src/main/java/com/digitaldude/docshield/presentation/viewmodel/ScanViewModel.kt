package com.digitaldude.docshield.presentation.viewmodel

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
    private val addDocumentUseCase: AddDocumentUseCase
) : ViewModel() {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState : StateFlow<ScanState> = _scanState.asStateFlow()

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
}

sealed class ScanState{
    object Idle: ScanState()
    object Loading : ScanState()
    data class Success(val imageUri : String, val extractedText : String) : ScanState()
    data class Error(val message : String) : ScanState()
}