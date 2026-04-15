package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.digitaldude.docshield.domain.model.Document


private fun extractSnippet(text: String, query: String, contextChars: Int = 60): String? {
    if (query.isBlank() || text.isBlank()) return null
    val index = text.indexOf(query, ignoreCase = true)
    if (index == -1) return null
    val start = maxOf(0, index - contextChars)
    val end = minOf(text.length, index + query.length + contextChars)
    val prefix = if (start > 0) "..." else ""
    val suffix = if (end < text.length) "..." else ""
    return "$prefix${text.substring(start, end)}$suffix"
}

@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToScan: () -> Unit,
    viewModel: DocumentViewModel = koinViewModel()
) {
    val documents by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuerry.collectAsStateWithLifecycle()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "DocShield",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigateToScan() }) {
            Text("Skeniraj dokument")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Pretraži dokumente...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(documents, key = { it.id }) { document ->
                DocumentCard(
                    document = document,
                    searchQuery = searchQuery,
                    onClick = { onNavigateToDetail(document.id) }
                )
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: Document,
    searchQuery : String,
    onClick: () -> Unit
) {

    val snippet = extractSnippet(document.extractedText, searchQuery)
    val highlightColor = MaterialTheme.colorScheme.primary


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = document.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(text = document.category, fontSize = 13.sp)
        }
    }
}




