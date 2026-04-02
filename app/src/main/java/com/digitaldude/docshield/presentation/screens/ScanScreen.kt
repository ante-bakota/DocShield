package com.digitaldude.docshield.presentation.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

  //  val scanner = remember { DocumentScannerDataSource(activity) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                Text(
                    text = state.extractedText.ifEmpty { "Nije pronaden nijedan tekst" },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.saveDocument(state.extractedText, state.imageUri) }){
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