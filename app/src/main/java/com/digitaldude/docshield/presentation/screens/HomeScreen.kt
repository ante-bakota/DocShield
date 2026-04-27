package com.digitaldude.docshield.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import org.koin.androidx.compose.koinViewModel


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
    onNavigateToImportPdf: () -> Unit,
    viewModel: DocumentViewModel = koinViewModel()
) {
    val documents by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuerry.collectAsStateWithLifecycle()

    // var — must be mutable so we can toggle it on FAB tap.
    // remember — survives recomposition; without it, state resets every recompose.
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            SpeedDialFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onScanClick = {
                    isFabExpanded = false
                    onNavigateToScan()
                },
                onImportPdfClick = {
                    isFabExpanded = false
                    onNavigateToImportPdf()
                }
            )
        }
    ) { paddingValues ->

        // Box lets us stack layers: content beneath, scrim on top.
        // Column alone can't stack — it only arranges children vertically in sequence.
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // paddingValues = space Scaffold reserves for FAB + system bars.
                    // Without this, the bottom of the list would hide behind the FAB.
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "DocShield",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
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

            // Scrim: drawn on top of content (second child in Box = higher z-order).
            // AnimatedVisibility fades it in/out instead of popping abruptly.
            // Tap on scrim closes the SpeedDial without navigating anywhere.
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { isFabExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun SpeedDialFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onScanClick: () -> Unit,
    onImportPdfClick: () -> Unit
) {
    // animateFloatAsState smoothly interpolates the rotation value between 0° and 45°.
    // The + icon at 45° visually becomes an ×.
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini FABs animate in when expanded, animate out when collapsed.
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Uvezi PDF", style = MaterialTheme.typography.labelLarge)
                    SmallFloatingActionButton(onClick = onImportPdfClick) {
                        Text("PDF", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Skeniraj dokument", style = MaterialTheme.typography.labelLarge)
                    SmallFloatingActionButton(onClick = onScanClick) {
                        Icon(Icons.Default.Add, contentDescription = "Skeniraj")
                    }
                }
            }
        }

        FloatingActionButton(onClick = onToggle) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Zatvori" else "Dodaj dokument",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun DocumentCard(
    document: Document,
    searchQuery: String,
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
