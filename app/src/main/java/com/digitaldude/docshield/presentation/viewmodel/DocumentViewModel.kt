package com.digitaldude.docshield.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.GetDocumentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val addDocumentUseCase: AddDocumentUseCase
) : ViewModel() {

    val documents: StateFlow<List<Document>> = getDocumentsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private val _searchQuerry = MutableStateFlow("")
    val searchQuerry : StateFlow<String> = _searchQuerry.asStateFlow()

    val filteredDocuments : StateFlow<List<Document>> = combine(
        documents, _searchQuerry
    ) { docs, querry ->
        if (querry.isBlank()) docs
        else docs.filter { doc ->
            doc.title.contains(querry, ignoreCase = true) ||
                    doc.extractedText.contains(querry, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query : String){
        _searchQuerry.value = query
    }
    fun dodajDokument(title: String, category: String) {
        viewModelScope.launch {
            addDocumentUseCase(Document(title = title, category = category))
        }
    }

    fun getDocumentById(id : Long) : Document? {
        return documents.value.find{ it.id == id}
    }
}