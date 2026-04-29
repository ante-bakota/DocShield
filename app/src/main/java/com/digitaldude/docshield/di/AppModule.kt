package com.digitaldude.docshield.di

import com.digitaldude.docshield.data.ml.GeminiNanoDataSource
import com.digitaldude.docshield.data.local.BiometricAuthManager
import com.digitaldude.docshield.data.local.DatabaseKeyManager
import com.digitaldude.docshield.data.local.DocShieldDatabase
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.data.ml.LlmInferenceDataSource
import com.digitaldude.docshield.data.ml.PdfTextExtractionDataSource
import com.digitaldude.docshield.data.ml.RuleBasedCategorizerDataSource
import com.digitaldude.docshield.data.ml.TextRecognitionDataSource
import com.digitaldude.docshield.data.repository.AiRepositoryImpl
import com.digitaldude.docshield.data.repository.DocumentRepositoryImpl
import com.digitaldude.docshield.data.repository.ScanRepositoryImpl
import com.digitaldude.docshield.domain.repository.AiRepository
import com.digitaldude.docshield.domain.repository.DocumentRepository
import com.digitaldude.docshield.domain.repository.ScanRepository
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.CategorizeDocumentUseCase
import com.digitaldude.docshield.domain.usecase.ExtractTextUseCase
import com.digitaldude.docshield.domain.usecase.GetDocumentsUseCase
import com.digitaldude.docshield.presentation.viewmodel.AuthViewModel
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import com.digitaldude.docshield.presentation.viewmodel.ImportPdfViewModel
import com.digitaldude.docshield.presentation.viewmodel.ScanViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel { DocumentViewModel(get(), get()) }
    viewModel{ AuthViewModel(get()) }
    viewModel{ ScanViewModel(get(), get(), get()) }
    viewModel{ ImportPdfViewModel(androidContext(), get(), get(), get()) }


    //Scanner
    single{ DocumentScannerDataSource(get()) }
    single{ TextRecognitionDataSource(get()) }
    single{ PdfTextExtractionDataSource(get(), get()) }


    single{ DatabaseKeyManager(get()) }
    single{ DocShieldDatabase.create(androidContext(), get<DatabaseKeyManager>().getOrCreatePassphrase()) }

    //AUth
    single{ BiometricAuthManager(get()) }

    // Repositories
    single<DocumentRepository> { DocumentRepositoryImpl(get<DocShieldDatabase>().documentDao()) }
    single<ScanRepository> { ScanRepositoryImpl(get()) }
    single<AiRepository> { AiRepositoryImpl(get(), get(), get()) }

    // AI data sources
    single { LlmInferenceDataSource(androidContext()) }
    single { GeminiNanoDataSource(androidContext()) }
    single { RuleBasedCategorizerDataSource() }

    // Use cases
    factory { GetDocumentsUseCase(get()) }
    factory { AddDocumentUseCase(get()) }
    factory { ExtractTextUseCase(get()) }
    factory { CategorizeDocumentUseCase(get()) }

}