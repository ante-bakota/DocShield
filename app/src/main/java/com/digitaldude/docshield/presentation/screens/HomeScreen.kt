package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToScan: () -> Unit,
    viewModel: DocumentViewModel = koinViewModel()
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "DocShield",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { viewModel.dodajDokument("Test dokument", "Ostalo") }) {
            Text("Dodaj dokument")
        }
        Button(onClick = {onNavigateToScan()}) {
            Text("Skeniraj dokument")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(documents) { document ->
                Text(
                    text = "${document.id}. ${document.title} — ${document.category}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDetail(document.id) }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }

    }

