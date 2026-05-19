package com.digitaldude.docshield.presentation.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.digitaldude.docshield.domain.model.DocumentType
import com.digitaldude.docshield.presentation.components.CategoryChip
import com.digitaldude.docshield.presentation.components.VaultBackground
import com.digitaldude.docshield.presentation.components.VaultCard
import com.digitaldude.docshield.presentation.util.cleanOcrText
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import com.digitaldude.docshield.ui.theme.DocLavender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailScreen(
    documentId: Long,
    onBack: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val document = documents.find { it.id == documentId }
    var ocrExpanded by remember { mutableStateOf(false) }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { paddingValues ->
        VaultBackground {
            val currentDocument = document
            if (currentDocument == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VaultCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Document not found",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailHeader(title = currentDocument.title, onBack = onBack)

                    val date = remember(currentDocument.dateAdded) {
                        SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault())
                            .format(Date(currentDocument.dateAdded))
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
                                CategoryChip(text = currentDocument.category)
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (currentDocument.documentType == DocumentType.PDF) "PDF document" else "Scanned document",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentDocument.imageUris.isNotEmpty()) {
                        VaultCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                val pagerState = rememberPagerState { currentDocument.imageUris.size }
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth()
                                ) { page ->
                                    AsyncImage(
                                        model = currentDocument.imageUris[page],
                                        contentDescription = "Page ${page + 1}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.large),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }

                                if (currentDocument.imageUris.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        repeat(currentDocument.imageUris.size) { index ->
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
                    }

                    val cleanedOcr = cleanOcrText(currentDocument.extractedText)
                    if (cleanedOcr.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { ocrExpanded = !ocrExpanded },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(if (ocrExpanded) "Hide OCR text" else "Show OCR text")
                        }
                        AnimatedVisibility(visible = ocrExpanded) {
                            VaultCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = cleanedOcr,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Document",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
