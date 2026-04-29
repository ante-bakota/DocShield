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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.digitaldude.docshield.presentation.viewmodel.ImportPdfState
import com.digitaldude.docshield.presentation.viewmodel.ImportPdfViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPdfScreen(
    onBack: () -> Unit,
    viewModel: ImportPdfViewModel = koinViewModel()
) {
    // collectAsStateWithLifecycle: čita StateFlow iz ViewModela i pretvara ga u Compose State.
    // "WithLifecycle" znači da prestaje slušati kad ekran nije vidljiv — štedi bateriju.
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiSuggestedTitle by viewModel.aiSuggestedTitle.collectAsStateWithLifecycle()
    val aiSuggestedCategory by viewModel.aiSuggestedCategory.collectAsStateWithLifecycle()

    // remember + mutableStateOf: lokalno UI stanje (je li dropdown otvoren).
    // Ovo NE ide u ViewModel jer je čisto vizualni detalj — ViewModel ne treba znati za to.
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Racun", "Zdravlje", "Osobne isprave", "Ostalo")

    // rememberLauncherForActivityResult: registrira file picker launcher.
    // Mora biti u Composableu (ne u ViewModelu) jer je vezan za Activity lifecycle.
    // GetContent = "otvori picker i daj mi URI odabranog fajla"
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.processPdf(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uvezi PDF") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // when na sealed classu — Kotlin garantira da su svi slučajevi pokriveni.
            // currentState je smart cast — unutar Ready bloka, compiler zna da je Ready.
            when (val currentState = state) {

                is ImportPdfState.Idle -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        "Odaberi PDF dokument s uređaja",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Odaberi PDF")
                    }
                }

                is ImportPdfState.Loading -> {
                    Spacer(modifier = Modifier.height(64.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analiziram PDF...", style = MaterialTheme.typography.bodyMedium)
                }

                is ImportPdfState.Ready -> {
                    val pagerState = rememberPagerState { currentState.pageImageUris.size }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        AsyncImage(
                            model = currentState.pageImageUris[page],
                            contentDescription = "Stranica ${page + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    if (currentState.pageImageUris.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(currentState.pageImageUris.size) { index ->
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

                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        label = { Text("Naziv dokumenta") },
                        placeholder = { Text("npr. Račun - HT") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = category,
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

                    // AI sekcija — isti pattern kao u ScanScreen
                    if (isAiLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Text("AI analizira...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.suggestWithAi(currentState.extractedText) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("AI prijedlog naziva i kategorije")
                        }
                    }

                    // let{} blok: izvršava se samo ako vrijednost nije null.
                    // Čišće od if (aiSuggestedTitle != null) { ... aiSuggestedTitle!! ... }
                    aiSuggestedTitle?.let { suggested ->
                        Text(
                            "Prijedlog naziva: $suggested",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedButton(
                            onClick = { viewModel.onTitleChanged(suggested) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Koristi predloženi naziv")
                        }
                    }

                    aiSuggestedCategory?.let { suggested ->
                        Text(
                            "Prijedlog kategorije: $suggested",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedButton(
                            onClick = { viewModel.onCategoryChanged(suggested) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Koristi predloženu kategoriju")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.saveDocument(onBack) },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Spremi dokument")
                    }

                    if (title.isBlank()) {
                        Text(
                            "Unesite naziv dokumenta za nastavak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Odaberi drugi PDF")
                    }
                }

                is ImportPdfState.Error -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        "Greška: ${currentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pokušaj ponovo")
                    }
                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Odustani")
                    }
                }
            }
        }
    }
}
