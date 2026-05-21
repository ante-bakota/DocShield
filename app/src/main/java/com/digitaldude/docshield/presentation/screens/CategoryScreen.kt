package com.digitaldude.docshield.presentation.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digitaldude.docshield.presentation.components.VaultBackground
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import com.digitaldude.docshield.ui.theme.DocLavender

// ─── Sort options ─────────────────────────────────────────────────────────────

private enum class SortOption(val label: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    NAME_ASC("Name A → Z"),
    NAME_DESC("Name Z → A")
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: String,
    onBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val allDocuments by viewModel.documents.collectAsStateWithLifecycle()

    var localSearch by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var showSortSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState()

    val categoryDocs = remember(allDocuments, category, localSearch, sortOption) {
        allDocuments
            .filter { it.category.equals(category, ignoreCase = true) }
            .let { docs ->
                if (localSearch.isBlank()) docs
                else docs.filter {
                    it.title.contains(localSearch, ignoreCase = true) ||
                        it.extractedText.contains(localSearch, ignoreCase = true)
                }
            }
            .let { docs ->
                when (sortOption) {
                    SortOption.DATE_DESC -> docs.sortedByDescending { it.dateAdded }
                    SortOption.DATE_ASC  -> docs.sortedBy { it.dateAdded }
                    SortOption.NAME_ASC  -> docs.sortedBy { it.title.lowercase() }
                    SortOption.NAME_DESC -> docs.sortedByDescending { it.title.lowercase() }
                }
            }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            DocShieldBottomBar(
                isFabExpanded = false,
                onFabToggle = {},
                isHomeSelected = false,
                onHomeClick = onBack,
                onSettingsClick = {}
            )
        }
    ) { paddingValues ->
        VaultBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top bar: back + title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = category,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search bar scoped to category
                PersistentSearchBar(
                    value = localSearch,
                    onValueChange = { localSearch = it },
                    onFocusChanged = {},
                    onClear = { localSearch = ""; focusManager.clearFocus() },
                    onSearch = {},
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Count + active sort label + filter button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${categoryDocs.size} document${if (categoryDocs.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (sortOption != SortOption.DATE_DESC) {
                            Text(
                                text = sortOption.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = DocLavender,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Filter button — tinted when non-default sort active
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (sortOption != SortOption.DATE_DESC)
                            DocLavender else DocLavender.copy(alpha = 0.75f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { showSortSheet = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Sort",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Document list
                if (categoryDocs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (localSearch.isBlank()) "No documents in this category"
                                   else "No results for \"$localSearch\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(categoryDocs, key = { it.id }) { doc ->
                            DocumentCard(
                                document = doc,
                                searchQuery = localSearch,
                                onClick = { onNavigateToDetail(doc.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }

        // Sort bottom sheet
        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sheetState
            ) {
                SortSheet(
                    current = sortOption,
                    onSelect = {
                        sortOption = it
                        showSortSheet = false
                    }
                )
            }
        }
    }
}

// ─── Sort sheet content ───────────────────────────────────────────────────────

@Composable
private fun SortSheet(
    current: SortOption,
    onSelect: (SortOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        SortOption.entries.forEach { option ->
            val selected = option == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option) }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) DocLavender
                            else MaterialTheme.colorScheme.onSurface
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = DocLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
