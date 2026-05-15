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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel


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
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val documents by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuerry.collectAsStateWithLifecycle()

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

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DocShield",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search documents...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (documents.isEmpty() && searchQuery.isBlank()) {
                    EmptyState()
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

            // scrim drawn on top of content when SpeedDial is open
            // tap anywhere on it to close the menu without navigating
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.93f)
                                )
                            )
                        )
                        .clickable { isFabExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No documents yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap + to scan or import a PDF",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    // animateFloatAsState smoothly interpolates the rotation value between 0 and 45 degrees
    // the + icon at 45 degrees visually becomes an x
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Import PDF",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    SmallFloatingActionButton(onClick = onImportPdfClick) {
                        Text("PDF", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Scan document",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    SmallFloatingActionButton(onClick = onScanClick) {
                        Icon(Icons.Default.Add, contentDescription = "Scan")
                    }
                }
            }
        }

        FloatingActionButton(onClick = onToggle) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close" else "Add document",
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
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // thumbnail — shown only if the document has at least one image
            if (document.imageUris.isNotEmpty()) {
                AsyncImage(
                    model = document.imageUris.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // category badge — uses primaryContainer so it reads as a tonal chip
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = document.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // show the matched OCR snippet when the user is searching
                if (snippet != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val annotated = buildAnnotatedString {
                        val lower = snippet.lowercase()
                        val queryLower = searchQuery.lowercase()
                        var cursor = 0
                        while (cursor < snippet.length) {
                            val hit = lower.indexOf(queryLower, cursor)
                            if (hit == -1) {
                                append(snippet.substring(cursor))
                                break
                            }
                            append(snippet.substring(cursor, hit))
                            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                                append(snippet.substring(hit, hit + searchQuery.length))
                            }
                            cursor = hit + searchQuery.length
                        }
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
