package com.digitaldude.docshield.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.digitaldude.docshield.ui.theme.DocAmberBg
import com.digitaldude.docshield.ui.theme.DocGray
import com.digitaldude.docshield.ui.theme.DocGrayBg
import com.digitaldude.docshield.ui.theme.DocLavender
import com.digitaldude.docshield.ui.theme.DocLavenderBg
import com.digitaldude.docshield.ui.theme.DocLavenderDark
import com.digitaldude.docshield.ui.theme.DocTeal
import com.digitaldude.docshield.ui.theme.DocTealBg
import com.digitaldude.docshield.ui.theme.DocTealDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Match type ───────────────────────────────────────────────────────────────

private enum class MatchType { TITLE, CONTENT, BOTH }

private fun getMatchType(document: Document, query: String): MatchType {
    val titleMatch = document.title.contains(query, ignoreCase = true)
    val contentMatch = document.extractedText.contains(query, ignoreCase = true)
    return when {
        titleMatch && contentMatch -> MatchType.BOTH
        titleMatch -> MatchType.TITLE
        else -> MatchType.CONTENT
    }
}

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

private fun countOcrMatches(text: String, query: String): Int {
    if (query.isBlank() || text.isBlank()) return 0
    var count = 0
    var start = 0
    val lower = text.lowercase()
    val queryLower = query.lowercase()
    while (true) {
        val index = lower.indexOf(queryLower, start)
        if (index == -1) break
        count++
        start = index + queryLower.length
    }
    return count
}

// ─── Category definitions ─────────────────────────────────────────────────────

private data class CategoryDef(
    val key: String,
    val displayName: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

private val categories = listOf(
    CategoryDef("Racun",          "Račun",          Icons.Outlined.Receipt,       DocAmberBg,    DocAmber),
    CategoryDef("Zdravlje",       "Zdravlje",        Icons.Outlined.LocalHospital, DocTealBg,     DocTealDark),
    CategoryDef("Osobne isprave", "Osobne isprave",  Icons.Outlined.Badge,         DocLavenderBg, DocLavenderDark),
    CategoryDef("Ostalo",         "Ostalo",          Icons.Outlined.FolderOpen,    DocGrayBg,     DocGray)
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    documentScannerDataSource: DocumentScannerDataSource,
    onBeforeExternalLaunch: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToScan: (List<String>) -> Unit,
    onNavigateToImportPdf: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val searchResults by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val recentDocuments by viewModel.recentDocuments.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuerry.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()

    var isFabExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isSearchActive || searchQuery.isNotBlank()) {
        viewModel.onSearchQueryChanged("")
        isSearchActive = false
        focusManager.clearFocus()
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            DocShieldBottomBar(
                isFabExpanded = isFabExpanded,
                onFabToggle = { isFabExpanded = !isFabExpanded },
                isHomeSelected = true,
                onHomeClick = {},
                onSettingsClick = {}
            )
        }
    ) { paddingValues ->
        VaultBackground {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                Column(modifier = Modifier.fillMaxSize()) {
                    // Title
                    Text(
                        text = "DocShield",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )

                    // Persistent search bar
                    PersistentSearchBar(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        onFocusChanged = { isSearchActive = it },
                        onClear = {
                            viewModel.onSearchQueryChanged("")
                            isSearchActive = false
                            focusManager.clearFocus()
                        },
                        onSearch = { query ->
                            if (query.isNotBlank()) viewModel.addToSearchHistory(query)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Content switches based on search state
                    when {
                        searchQuery.isNotBlank() -> SearchResultsList(
                            results = searchResults,
                            query = searchQuery,
                            onDocumentClick = { onNavigateToDetail(it.id) }
                        )

                        isSearchActive -> RecentSearchesList(
                            history = searchHistory,
                            onHistoryItemClick = { query ->
                                viewModel.onSearchQueryChanged(query)
                            },
                            onRemoveItem = viewModel::removeFromSearchHistory,
                            onClearAll = viewModel::clearSearchHistory
                        )

                        else -> HomeContent(
                            categoryCounts = categoryCounts,
                            recentDocuments = recentDocuments,
                            onCategoryClick = onNavigateToCategory,
                            onDocumentClick = { onNavigateToDetail(it.id) }
                        )
                    }
                }

                // Scrim when speed dial open
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
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.76f)
                                    )
                                )
                            )
                            .clickable { isFabExpanded = false }
                    )
                }

                // Speed dial options — anchored above bottom nav
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 88.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FabOption(
                            label = "Import PDF",
                            icon = Icons.Outlined.UploadFile,
                            onClick = {
                                isFabExpanded = false
                                onNavigateToImportPdf()
                            }
                        )
                        FabOption(
                            label = "Scan document",
                            icon = Icons.Outlined.CameraAlt,
                            onClick = {
                                isFabExpanded = false
                                documentScannerDataSource.startScan(
                                    onBeforeLaunch = onBeforeExternalLaunch
                                ) { uris ->
                                    if (uris.isNotEmpty()) onNavigateToScan(uris.filterNotNull())
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Bottom navigation ────────────────────────────────────────────────────────

@Composable
fun DocShieldBottomBar(
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    isHomeSelected: Boolean,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fabRotation by animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    selected = isHomeSelected,
                    onClick = onHomeClick
                )
                Spacer(modifier = Modifier.width(56.dp))
                BottomNavItem(
                    icon = Icons.Outlined.Settings,
                    label = "Settings",
                    selected = false,
                    onClick = onSettingsClick
                )
            }
        }

        // FAB elevated above the surface — positioned at top of the Box
        FloatingActionButton(
            onClick = onFabToggle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(bottom = 10.dp)
                .size(52.dp),
            containerColor = DocLavender,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isFabExpanded) "Close" else "Add document",
                modifier = Modifier.rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) DocTeal.copy(alpha = 0.18f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) DocTealDark else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) DocTealDark else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
fun PersistentSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = if (isFocused) DocLavender else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        isFocused = state.isFocused
                        onFocusChanged(state.isFocused)
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(DocLavender),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(value) }),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Search documents...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─── Home content — folders + recent ─────────────────────────────────────────

@Composable
private fun HomeContent(
    categoryCounts: Map<String, Int>,
    recentDocuments: List<Document>,
    onCategoryClick: (String) -> Unit,
    onDocumentClick: (Document) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Categories")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* TODO: create folder dialog */ }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreateNewFolder,
                        contentDescription = "New folder",
                        tint = DocLavender,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New Folder",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DocLavender
                    )
                }
            }
        }
        item {
            FolderGrid(
                categoryCounts = categoryCounts,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        if (recentDocuments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
            items(recentDocuments, key = { it.id }) { doc ->
                DocumentCard(
                    document = doc,
                    searchQuery = "",
                    onClick = { onDocumentClick(doc) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    )
}



@Composable
private fun FolderGrid(
    categoryCounts: Map<String, Int>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { cat ->
                    FolderCard(
                        def = cat,
                        count = categoryCounts[cat.key] ?: 0,
                        onClick = { onCategoryClick(cat.key) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun FolderCard(
    def: CategoryDef,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VaultCard(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(def.bgColor.copy(alpha = 0.45f))
            .border(
                BorderStroke(1.5.dp, def.iconColor.copy(alpha = 0.35f)),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(def.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = def.icon,
                    contentDescription = null,
                    tint = def.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = def.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count documents",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Search results ───────────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(
    results: List<Document>,
    query: String,
    onDocumentClick: (Document) -> Unit
) {
    if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No results for \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Text(
        text = "${results.size} result${if (results.size != 1) "s" else ""} for \"$query\"",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(results, key = { it.id }) { doc ->
            DocumentCard(
                document = doc,
                searchQuery = query,
                onClick = { onDocumentClick(doc) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─── Recent searches ──────────────────────────────────────────────────────────

@Composable
private fun RecentSearchesList(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearAll: () -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recent searches",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT SEARCHES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "Clear all",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DocLavender,
                modifier = Modifier.clickable(onClick = onClearAll)
            )
        }

        history.forEach { query ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryItemClick(query) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { onRemoveItem(query) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─── Document card ────────────────────────────────────────────────────────────

@Composable
fun DocumentCard(
    document: Document,
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val date = remember(document.dateAdded) {
        SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(Date(document.dateAdded))
    }

    val matchType = if (searchQuery.isBlank()) null else getMatchType(document, searchQuery)
    val snippet = if (searchQuery.isBlank()) null else extractSnippet(document.extractedText, searchQuery)
    val ocrMatchCount = if (searchQuery.isBlank()) 0 else countOcrMatches(document.extractedText, searchQuery)

    VaultCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DocumentThumb(document = document)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title — bold, highlighted if title match
                    if (matchType == MatchType.TITLE || matchType == MatchType.BOTH) {
                        HighlightedText(
                            text = document.title,
                            query = searchQuery,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            baseColor = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Date
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Category
                    CategoryChip(text = document.category)
                }
            }

            // OCR snippet section
            if (snippet != null && (matchType == MatchType.CONTENT || matchType == MatchType.BOTH)) {
                Spacer(modifier = Modifier.height(8.dp))
                OcrSnippetBox(
                    snippet = snippet,
                    query = searchQuery,
                    matchType = matchType,
                    extraMatchCount = ocrMatchCount - 1
                )
            }
        }
    }
}

@Composable
private fun OcrSnippetBox(
    snippet: String,
    query: String,
    matchType: MatchType,
    extraMatchCount: Int
) {
    val label = when (matchType) {
        MatchType.BOTH    -> "Title & content match"
        MatchType.CONTENT -> "Found in content"
        else              -> ""
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 6.dp, topEnd = 6.dp, bottomEnd = 6.dp),
        color = DocTeal.copy(alpha = 0.08f)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Teal left border
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(
                        if (extraMatchCount > 0) 84.dp else 60.dp
                    )
                    .background(DocTealDark)
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DocTealDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                HighlightedText(
                    text = snippet,
                    query = query,
                    style = MaterialTheme.typography.bodySmall,
                    baseColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (extraMatchCount > 0) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = DocTealDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "+$extraMatchCount more match${if (extraMatchCount > 1) "es" else ""} in this document",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DocTealDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    baseColor: Color
) {
    val annotated = buildAnnotatedString {
        if (query.isBlank()) {
            withStyle(SpanStyle(color = baseColor)) { append(text) }
            return@buildAnnotatedString
        }
        val lower = text.lowercase()
        val queryLower = query.lowercase()
        var cursor = 0
        while (cursor < text.length) {
            val hit = lower.indexOf(queryLower, cursor)
            if (hit == -1) {
                withStyle(SpanStyle(color = baseColor)) { append(text.substring(cursor)) }
                break
            }
            if (hit > cursor) {
                withStyle(SpanStyle(color = baseColor)) { append(text.substring(cursor, hit)) }
            }
            withStyle(
                SpanStyle(
                    color = DocTealDark,
                    fontWeight = FontWeight.Bold,
                    background = DocTeal.copy(alpha = 0.22f)
                )
            ) {
                append(text.substring(hit, hit + query.length))
            }
            cursor = hit + query.length
        }
    }
    Text(text = annotated, style = style)
}

@Composable
private fun DocumentThumb(document: Document) {
    if (document.imageUris.isNotEmpty()) {
        AsyncImage(
            model = document.imageUris.first(),
            contentDescription = null,
            modifier = Modifier
                .size(width = 58.dp, height = 72.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(DocAmber, DocLavender))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ─── FAB speed dial option ────────────────────────────────────────────────────

@Composable
private fun FabOption(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = DocLavender,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}
