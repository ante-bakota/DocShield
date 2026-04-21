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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.digitaldude.docshield.presentation.util.cleanOcrText
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
    var ocrExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
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


         if (document.imageUris.isNotEmpty()){
             Spacer(modifier = Modifier.height(12.dp))
             Text(text = "Skenirani dokument :" , fontWeight = FontWeight.SemiBold)
             Spacer(modifier = Modifier.height(4.dp))

             val pagerState = rememberPagerState() { document.imageUris.size }

             HorizontalPager(
                 state = pagerState,
                 modifier = Modifier.fillMaxWidth()
             ) {
                 page ->
                    AsyncImage(
                        model = document.imageUris[page],
                        contentDescription = "Stranica ${page + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillWidth                    )
             }

             if (document.imageUris.size > 1) {
                 Row(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(vertical = 8.dp),
                     horizontalArrangement = Arrangement.Center
                 ) {
                     repeat(document.imageUris.size) { index ->
                         val isSelected = pagerState.currentPage == index
                         Box(
                             modifier = Modifier
                                 .padding(horizontal = 4.dp)
                                 .size(if (isSelected) 10.dp else 7.dp)
                                 .clip(CircleShape)
                                 .background(
                                     if (isSelected) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                 )
                         )
                     }
                 }
             }
         }

            val cleanedOcr = cleanOcrText(document.extractedText)
            if (cleanedOcr.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { ocrExpanded = !ocrExpanded }) {
                    Text(if (ocrExpanded) "Sakrij OCR tekst" else "Prikaži OCR tekst")
                }
                AnimatedVisibility(visible = ocrExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = cleanedOcr,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
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
