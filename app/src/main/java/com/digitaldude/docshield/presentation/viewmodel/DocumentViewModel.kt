package com.digitaldude.docshield.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldude.docshield.domain.model.Category
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.domain.usecase.AddCategoryUseCase
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.DeleteCategoryUseCase
import com.digitaldude.docshield.domain.usecase.GetCategoriesUseCase
import com.digitaldude.docshield.domain.usecase.GetDocumentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val addDocumentUseCase: AddDocumentUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
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

    val recentDocuments: StateFlow<List<Document>> = documents
        .map { docs -> docs.sortedByDescending { it.dateAdded }.take(6) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryCounts: StateFlow<Map<String, Int>> = documents
        .map { docs -> docs.groupBy { it.category }.mapValues { it.value.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Custom categories — persisted in Room, survives app restarts
    val customCategories: StateFlow<List<String>> = getCategoriesUseCase()
        .map { categories -> categories.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            addCategoryUseCase(Category(name = trimmed))
        }
    }

    fun removeCustomCategory(name: String) {
        viewModelScope.launch {
            deleteCategoryUseCase(name)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuerry.value = query
    }

    fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        _searchHistory.update { history ->
            (listOf(query) + history.filter { it != query }).take(10)
        }
    }

    fun removeFromSearchHistory(query: String) {
        _searchHistory.update { it.filter { item -> item != query } }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
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