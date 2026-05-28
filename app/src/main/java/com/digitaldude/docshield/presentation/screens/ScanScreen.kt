package com.digitaldude.docshield.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.digitaldude.docshield.presentation.components.CategoryChip
import com.digitaldude.docshield.presentation.components.FullScreenLoader
import com.digitaldude.docshield.presentation.components.InlineLoader
import com.digitaldude.docshield.presentation.components.SecureButton
import com.digitaldude.docshield.presentation.components.VaultBackground
import com.digitaldude.docshield.presentation.components.VaultCard
import com.digitaldude.docshield.presentation.viewmodel.ScanState
import com.digitaldude.docshield.presentation.viewmodel.ScanViewModel
import com.digitaldude.docshield.ui.theme.DocLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val viewModel: ScanViewModel = hiltViewModel()
    val scanState by viewModel.scanState.collectAsState()
    val aiSuggestedTitle by viewModel.aiSuggestedTitle.collectAsState()
    val aiSuggestedCategory by viewModel.aiSuggestedCategory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var documentTitle by remember { mutableStateOf("") }
    var documentCategory by remember { mutableStateOf("Other") }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Invoice", "Health", "Identity", "Other")

    zoomedImageUri?.let { uri ->
        BasicAlertDialog(
            onDismissRequest = { zoomedImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { zoomedImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Enlarged image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    when (val state = scanState) {
        ScanState.Loading -> FullScreenLoader(message = "Reading document...")

        is ScanState.Success -> {
            ScanSuccessContent(
                state = state,
                documentTitle = documentTitle,
                onDocumentTitleChange = { documentTitle = it },
                documentCategory = documentCategory,
                onDocumentCategoryChange = { documentCategory = it },
                categories = categories,
                isAiLoading = isAiLoading,
                aiSuggestedTitle = aiSuggestedTitle,
                aiSuggestedCategory = aiSuggestedCategory,
                onSuggestWithAi = { viewModel.suggestWithAi(state.extractedText) },
                onApplyAiSuggestions = {
                    aiSuggestedTitle?.let { documentTitle = it }
                    aiSuggestedCategory?.let { documentCategory = it }
                },
                onSaveDocument = {
                    viewModel.saveDocument(
                        title = documentTitle,
                        extractedText = state.extractedText,
                        imageUris = state.imageUris,
                        category = documentCategory,
                        onSaved = onNavigateHome
                    )
                },
                onScanNew = onBack,
                onImageClick = { zoomedImageUri = it }
            )
        }

        is ScanState.Error -> {
            VaultBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VaultCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Something went wrong",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            SecureButton(
                                onClick = { viewModel.retryOcr() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Try again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScanSuccessContent(
    state: ScanState.Success,
    documentTitle: String,
    onDocumentTitleChange: (String) -> Unit,
    documentCategory: String,
    onDocumentCategoryChange: (String) -> Unit,
    categories: List<String>,
    isAiLoading: Boolean,
    aiSuggestedTitle: String?,
    aiSuggestedCategory: String?,
    onSuggestWithAi: () -> Unit,
    onApplyAiSuggestions: () -> Unit,
    onSaveDocument: () -> Unit,
    onScanNew: () -> Unit,
    onImageClick: (String) -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState { state.imageUris.size }
    val listState = rememberLazyListState()
    val isCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 60
        }
    }
    val imageHeight by animateDpAsState(
        targetValue = if (isCollapsed) 150.dp else 320.dp,
        label = "imageHeight"
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecureButton(
                        onClick = onSaveDocument,
                        enabled = documentTitle.isNotBlank() && !isAiLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save document")
                    }
                    TextButton(
                        onClick = onScanNew,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan another document")
                    }
                }
            }
        }
    ) { paddingValues ->
        VaultBackground {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Review scan",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    VaultCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(imageHeight)
                                    .animateContentSize()
                            ) { page ->
                                AsyncImage(
                                    model = state.imageUris[page],
                                    contentDescription = "Page ${page + 1}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onImageClick(state.imageUris[page]) },
                                    contentScale = ContentScale.Crop
                                )
                            }

                            if (state.imageUris.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(state.imageUris.size) { index ->
                                        val selected = pagerState.currentPage == index
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .size(if (selected) 10.dp else 8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selected) DocLavender
                                                    else MaterialTheme.colorScheme.outlineVariant
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    VaultCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Document details",
                                style = MaterialTheme.typography.titleLarge
                            )
                            OutlinedTextField(
                                value = documentTitle,
                                onValueChange = onDocumentTitleChange,
                                label = { Text("Document name") },
                                placeholder = { Text("e.g. Receipt - Harvey Norman") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )

                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = documentCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                onDocumentCategoryChange(category)
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (documentTitle.isBlank()) {
                                Text(
                                    text = "Enter a document name to save.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {
                    VaultCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "AI suggestion",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                CategoryChip(text = "Local assist")
                            }

                            if (isAiLoading) {
                                InlineLoader(message = "Analyzing document...")
                            } else {
                                FilledTonalButton(
                                    onClick = onSuggestWithAi,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Generate suggestion")
                                }
                            }

                            if (aiSuggestedTitle != null || aiSuggestedCategory != null) {
                                HorizontalDivider()
                                aiSuggestedTitle?.let {
                                    SuggestionRow(label = "Name", value = it)
                                }
                                aiSuggestedCategory?.let {
                                    SuggestionRow(label = "Category", value = it)
                                }
                                Button(
                                    onClick = onApplyAiSuggestions,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Apply")
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SuggestionRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.38f)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
