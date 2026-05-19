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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.domain.model.Document
import com.digitaldude.docshield.presentation.components.CategoryChip
import com.digitaldude.docshield.presentation.components.VaultBackground
import com.digitaldude.docshield.presentation.components.VaultCard
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import com.digitaldude.docshield.ui.theme.DocAmber
import com.digitaldude.docshield.ui.theme.DocLavender
import com.digitaldude.docshield.ui.theme.DocTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    documentScannerDataSource: DocumentScannerDataSource,
    onBeforeExternalLaunch: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToScan: (List<String>) -> Unit,
    onNavigateToImportPdf: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val documents by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuerry.collectAsStateWithLifecycle()
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            SpeedDialFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onScanClick = {
                    isFabExpanded = false
                    documentScannerDataSource.startScan(
                        onBeforeLaunch = onBeforeExternalLaunch
                    ) { uris ->
                        if (uris.isNotEmpty()) onNavigateToScan(uris.filterNotNull())
                    }
                },
                onImportPdfClick = {
                    isFabExpanded = false
                    onNavigateToImportPdf()
                }
            )
        }
    ) { paddingValues ->
        VaultBackground {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Header(documentCount = documents.size)
                    SearchField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged
                    )

                    if (documents.isEmpty() && searchQuery.isBlank()) {
                        EmptyState(modifier = Modifier.weight(1f))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(documents, key = { it.id }) { document ->
                                DocumentCard(
                                    document = document,
                                    searchQuery = searchQuery,
                                    onClick = { onNavigateToDetail(document.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(88.dp)) }
                        }
                    }
                }

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
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.82f)
                                    )
                                )
                            )
                            .clickable { isFabExpanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(documentCount: Int) {
    VaultCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.linearGradient(
                            listOf(DocAmber, DocLavender, DocTeal)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("D", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DocShield",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$documentCount protected documents",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Search documents...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
            focusedBorderColor = DocLavender,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        VaultCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(DocAmber.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PDF", color = DocAmber, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "No documents yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap + to scan a document or import a PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                FabAction(label = "Import PDF", shortLabel = "PDF", onClick = onImportPdfClick)
                FabAction(label = "Scan document", shortLabel = "+", onClick = onScanClick)
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = DocLavender,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close" else "Add document",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun FabAction(label: String, shortLabel: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 6.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape
        ) {
            Text(shortLabel, style = MaterialTheme.typography.labelMedium)
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
    val date = remember(document.dateAdded) {
        SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(Date(document.dateAdded))
    }

    VaultCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DocumentThumb(document = document)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(text = document.category)
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (snippet != null) {
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
                            withStyle(SpanStyle(color = DocTeal, fontWeight = FontWeight.Bold)) {
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

@Composable
private fun DocumentThumb(document: Document) {
    if (document.imageUris.isNotEmpty()) {
        AsyncImage(
            model = document.imageUris.first(),
            contentDescription = null,
            modifier = Modifier
                .size(width = 58.dp, height = 72.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 72.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(Brush.linearGradient(listOf(DocAmber, DocLavender))),
            contentAlignment = Alignment.Center
        ) {
            Text("PDF", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}
