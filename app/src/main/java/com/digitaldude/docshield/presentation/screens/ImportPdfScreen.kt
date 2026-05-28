package com.digitaldude.docshield.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.digitaldude.docshield.presentation.components.CategoryChip
import com.digitaldude.docshield.presentation.components.FullScreenLoader
import com.digitaldude.docshield.presentation.components.InlineLoader
import com.digitaldude.docshield.presentation.components.SecureButton
import com.digitaldude.docshield.presentation.components.VaultBackground
import com.digitaldude.docshield.presentation.components.VaultCard
import com.digitaldude.docshield.presentation.viewmodel.ImportPdfState
import com.digitaldude.docshield.presentation.viewmodel.ImportPdfViewModel
import com.digitaldude.docshield.ui.theme.DocAmber
import com.digitaldude.docshield.ui.theme.DocLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPdfScreen(
    onBack: () -> Unit,
    onBeforeExternalLaunch: () -> Unit,
    viewModel: ImportPdfViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiSuggestedTitle by viewModel.aiSuggestedTitle.collectAsStateWithLifecycle()
    val aiSuggestedCategory by viewModel.aiSuggestedCategory.collectAsStateWithLifecycle()
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Invoice", "Health", "Identity", "Other")

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.processPdf(it) }
    }

    when (val currentState = state) {
        ImportPdfState.Loading -> FullScreenLoader(message = "Analyzing PDF...")

        else -> {
            Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { paddingValues ->
                VaultBackground {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ImportHeader(onBack = onBack)

                        when (currentState) {
                            ImportPdfState.Idle -> {
                                VaultCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(18.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(88.dp)
                                                .clip(MaterialTheme.shapes.extraLarge)
                                                .background(DocAmber.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.UploadFile,
                                                contentDescription = null,
                                                tint = DocAmber,
                                                modifier = Modifier.size(52.dp)
                                            )
                                        }
                                        Text(
                                            text = "Choose a PDF from your device",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "DocShield will extract text and prepare a secure document preview.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        SecureButton(
                                            onClick = {
                                                onBeforeExternalLaunch()
                                                pdfPickerLauncher.launch("application/pdf")
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Choose PDF")
                                        }
                                    }
                                }
                            }

                            is ImportPdfState.Ready -> {
                                val pagerState = rememberPagerState { currentState.pageImageUris.size }

                                VaultCard(modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxWidth()
                                        ) { page ->
                                            AsyncImage(
                                                model = currentState.pageImageUris[page],
                                                contentDescription = "Page ${page + 1}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.large),
                                                contentScale = ContentScale.FillWidth
                                            )
                                        }

                                        if (currentState.pageImageUris.size > 1) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                repeat(currentState.pageImageUris.size) { index ->
                                                    val selected = pagerState.currentPage == index
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 4.dp)
                                                            .size(if (selected) 10.dp else 7.dp)
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
                                            value = title,
                                            onValueChange = viewModel::onTitleChanged,
                                            label = { Text("Document name") },
                                            placeholder = { Text("e.g. Receipt - HT") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = MaterialTheme.shapes.medium
                                        )

                                        ExposedDropdownMenuBox(
                                            expanded = categoryDropdownExpanded,
                                            onExpandedChange = { categoryDropdownExpanded = it }
                                        ) {
                                            OutlinedTextField(
                                                value = category,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Category") },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = categoryDropdownExpanded
                                                    )
                                                },
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillMaxWidth(),
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            ExposedDropdownMenu(
                                                expanded = categoryDropdownExpanded,
                                                onDismissRequest = { categoryDropdownExpanded = false }
                                            ) {
                                                categories.forEach { cat ->
                                                    DropdownMenuItem(
                                                        text = { Text(cat) },
                                                        onClick = {
                                                            viewModel.onCategoryChanged(cat)
                                                            categoryDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

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
                                            InlineLoader(message = "AI is analyzing...")
                                        } else {
                                            FilledTonalButton(
                                                onClick = { viewModel.suggestWithAi(currentState.extractedText) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                Text("Suggest name and category")
                                            }
                                        }

                                        if (aiSuggestedTitle != null || aiSuggestedCategory != null) {
                                            HorizontalDivider()
                                            aiSuggestedTitle?.let { suggested ->
                                                SuggestionRow(label = "Name", value = suggested)
                                                OutlinedButton(
                                                    onClick = { viewModel.onTitleChanged(suggested) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = MaterialTheme.shapes.medium
                                                ) {
                                                    Text("Use suggested name")
                                                }
                                            }
                                            aiSuggestedCategory?.let { suggested ->
                                                SuggestionRow(label = "Category", value = suggested)
                                                OutlinedButton(
                                                    onClick = { viewModel.onCategoryChanged(suggested) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = MaterialTheme.shapes.medium
                                                ) {
                                                    Text("Use suggested category")
                                                }
                                            }
                                        }
                                    }
                                }

                                SecureButton(
                                    onClick = { viewModel.saveDocument(onBack) },
                                    enabled = title.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save document")
                                }

                                if (title.isBlank()) {
                                    Text(
                                        text = "Enter a document name to continue.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.reset() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Choose another PDF")
                                }
                            }

                            is ImportPdfState.Error -> {
                                VaultCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "PDF import failed",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentState.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        SecureButton(
                                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Try again")
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.reset() },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            }

                            ImportPdfState.Loading -> Unit
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column {
            Text(
                text = "Import",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "PDF document",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SuggestionRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.34f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
