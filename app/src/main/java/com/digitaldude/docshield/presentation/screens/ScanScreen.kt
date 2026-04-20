package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.presentation.viewmodel.ScanState
import com.digitaldude.docshield.presentation.viewmodel.ScanViewModel
import org.koin.androidx.compose.koinViewModel

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
    val scrollState = rememberScrollState()
    val categories = listOf("Racun", "Zdravlje", "Osobne isprave", "Ostalo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when(val state = scanState){
            is ScanState.Idle -> {
                Button(onClick = {
                    documentScannerDataSource.startScan { uri ->
                        if(uri != null){
                            viewModel.onDocumentScanned(uri)
                        }
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
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = documentTitle,
                    onValueChange = {documentTitle = it},
                    label = {Text("Naziv dokumenta")},
                    placeholder = {Text("npr. Racun - Harwey Norman")},
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.suggestWithAi(state.extractedText) }) {
                    Text("AI prijedlog naziva")
                }
                aiSuggestedTitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AI prijedlog: $it", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { documentTitle = it }) {
                        Text("Koristi AI prijedlog")
                    }
                }

                aiSuggestedCategory?.let{
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AI prijedlog: $it", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { documentCategory = it}) {
                        Text("Koristi ai prijedlog za kategoriju")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveDocument(documentTitle, state.extractedText, state.imageUri, documentCategory) },
                    enabled = documentTitle.isNotBlank()
                ) {
                    Text("Spremi dokument")
                }
                Button(onClick = { viewModel.resetState() }) {
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