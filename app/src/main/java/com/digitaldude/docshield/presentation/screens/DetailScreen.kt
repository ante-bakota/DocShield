package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(
    documentId : Long,
    onBack: () -> Unit,
    viewModel: DocumentViewModel = koinViewModel()
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val document = documents.find { it.id == documentId }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
      Text(
          text = "Detalji dokumenta",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
      )
        Spacer(modifier = Modifier.height(16.dp))
        if (document != null) {
            Text(text = "ID: ${document.id}")
            Text(text = "Naziv: ${document.title}")
            Text(text = "Kategorija: ${document.category}")
        } else {
            Text(text = "Dokument nije pronađen.")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Natrag")
        }

    }
}
