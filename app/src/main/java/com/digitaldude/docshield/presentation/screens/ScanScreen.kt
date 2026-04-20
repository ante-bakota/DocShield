package com.digitaldude.docshield.presentation.screens

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.presentation.viewmodel.ScanState
import com.digitaldude.docshield.presentation.viewmodel.ScanViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(documentScannerDataSource: DocumentScannerDataSource){
    val viewModel: ScanViewModel = koinViewModel()
    val scanState by viewModel.scanState.collectAsState()
    val aiSuggestedTitle by viewModel.aiSuggestedTitle.collectAsState()
    val aiSuggestedCategory by viewModel.aiSuggestedCategory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var documentTitle by remember { mutableStateOf("") }
    var documentCategory by remember { mutableStateOf("Ostalo") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Racun", "Zdravlje", "Osobne isprave", "Ostalo")
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    zoomedImageUri?.let { uri ->
        BasicAlertDialog(
            onDismissRequest = { zoomedImageUri = null},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable{ zoomedImageUri = null},
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Uvecana slika",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when(val state = scanState){
            is ScanState.Idle -> {
                Button(onClick = {
                    documentScannerDataSource.startScan { uris ->
                        if (uris.isNotEmpty()) viewModel.onDocumentScanned(uris as List<String>)
                    }
                }) {
                    Text("Skeniraj dokument")
                }
            }
            is ScanState.Loading ->{
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Citam dokument")
            }
            is ScanState.Success -> {
                val pagerState = rememberPagerState() { state.imageUris.size }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    page ->
                    AsyncImage(
                        model = state.imageUris[page],
                        contentDescription = "Stranica ${page + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable{ zoomedImageUri = state.imageUris[page]},
                        contentScale = ContentScale.FillWidth
                    )
                }

                if(state.imageUris.size > 1){
                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ){
                        repeat(state.imageUris.size){ index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if(isSelected) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if(isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy( alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = documentTitle,
                    onValueChange = {documentTitle = it},
                    label = {Text("Naziv dokumenta")},
                    placeholder = {Text("npr. Racun - Harwey Norman")},
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = documentCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategorija") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    documentCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (isAiLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("AI analizira dokument...", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.suggestWithAi(state.extractedText) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AI prijedlog naziva i kategorije")
                    }
                }

                aiSuggestedTitle?.let { title ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Prijedlog naziva: $title",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedButton(
                        onClick = { documentTitle = title },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Koristi predloženi naziv")
                    }
                }

                aiSuggestedCategory?.let { category ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Prijedlog kategorije: $category",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedButton(
                        onClick = { documentCategory = category },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Koristi predloženu kategoriju")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.saveDocument(
                            documentTitle,
                            state.extractedText,
                            state.imageUris,
                            documentCategory
                        )
                    },
                    enabled = documentTitle.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Spremi dokument")
                }
                if (documentTitle.isBlank()) {
                    Text(
                        "Unesite naziv dokumenta za nastavak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skeniraj novi dokument")
                }
            }
            is ScanState.Error -> {
                Text("Greška: ${state.message}")
                Button(onClick = { viewModel.resetState() }) {
                    Text("Pokušaj ponovo")
                }
            }
        }
    }
}