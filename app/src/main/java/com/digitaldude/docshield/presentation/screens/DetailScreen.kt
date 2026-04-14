package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
            Text(text = document.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Kategorija: ${document.category}")
            Text(text = "Dodano: ${
                java.text.SimpleDateFormat("dd.MM.yyyy. HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(document.dateAdded))
            }")


            if(document.imageUri.isNotEmpty()){
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Skenirani dokument:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                AsyncImage(
                    model = document.imageUri,
                    contentDescription = "Skenirani dokument",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth
                )

            }
        } else {
            Text(text = "Dokument nije pronađen.")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Natrag")
        }

    }
}
