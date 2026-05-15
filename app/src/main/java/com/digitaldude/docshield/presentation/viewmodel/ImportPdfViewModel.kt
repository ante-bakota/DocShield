package com.digitaldude.docshield.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldude.docshield.data.ml.PdfTextExtractionDataSource
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.model.DocumentType
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.CategorizeDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImportPdfViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val pdfTextExtractionDataSource: PdfTextExtractionDataSource,
    private val categorizeDocumentUseCase: CategorizeDocumentUseCase,
    private val addDocumentUseCase: AddDocumentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ImportPdfState>(ImportPdfState.Idle)
    val state: StateFlow<ImportPdfState> = _state.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _category = MutableStateFlow("Ostalo")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiSuggestedTitle = MutableStateFlow<String?>(null)
    val aiSuggestedTitle: StateFlow<String?> = _aiSuggestedTitle.asStateFlow()

    private val _aiSuggestedCategory = MutableStateFlow<String?>(null)
    val aiSuggestedCategory: StateFlow<String?> = _aiSuggestedCategory.asStateFlow()

    fun onTitleChanged(value: String) { _title.value = value }
    fun onCategoryChanged(value: String) { _category.value = value }

    fun processPdf(uri: Uri) {
        viewModelScope.launch {
            _state.value = ImportPdfState.Loading
            try {
                // URI from the file picker has a temporary permission — copy to internal storage
                // because we need permanent access to the file even after the picker is dismissed
                val internalUri = copyPdfToInternalStorage(uri)
                val filePrefix = "pdf_${System.currentTimeMillis()}"
                val extractedText = pdfTextExtractionDataSource.extractText(internalUri)
                val pageImageUris = pdfTextExtractionDataSource.renderPagesToFiles(internalUri, filePrefix)
                _state.value = ImportPdfState.Ready(extractedText, pageImageUris)
            } catch (e: Exception) {
                _state.value = ImportPdfState.Error(e.message ?: "Greška pri obradi PDF-a")
            }
        }
    }

    fun suggestWithAi(extractedText: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val suggestion = categorizeDocumentUseCase(extractedText)
            _aiSuggestedTitle.value = suggestion.suggestedTitle
            _aiSuggestedCategory.value = suggestion.suggestedCategory
            _isAiLoading.value = false
        }
    }

    fun saveDocument(onSaved: () -> Unit) {
        val current = _state.value as? ImportPdfState.Ready ?: return
        viewModelScope.launch {
            addDocumentUseCase(
                Document(
                    title = _title.value.ifBlank { "PDF dokument" },
                    extractedText = current.extractedText,
                    imageUris = current.pageImageUris.map { it.toString() },
                    category = _category.value,
                    documentType = DocumentType.PDF
                )
            )
            onSaved()
        }
    }

    fun reset() {
        _state.value = ImportPdfState.Idle
        _title.value = ""
        _category.value = "Ostalo"
        _aiSuggestedTitle.value = null
        _aiSuggestedCategory.value = null
    }

    private fun copyPdfToInternalStorage(uri: Uri): Uri {
        val fileName = "pdf_${System.currentTimeMillis()}.pdf"
        val destFile = File(application.filesDir, fileName)
        application.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(destFile)
    }
}

sealed class ImportPdfState {
    object Idle : ImportPdfState()
    object Loading : ImportPdfState()
    data class Ready(val extractedText: String, val pageImageUris: List<Uri>) : ImportPdfState()
    data class Error(val message: String) : ImportPdfState()
}
