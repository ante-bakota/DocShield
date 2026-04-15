package com.digitaldude.docshield.di

import GeminiNanoDataSource
import com.digitaldude.docshield.data.local.BiometricAuthManager
import com.digitaldude.docshield.data.local.DatabaseKeyManager
import com.digitaldude.docshield.data.local.DocShieldDatabase
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.data.ml.TextRecognitionDataSource
import com.digitaldude.docshield.data.repository.DocumentRepositoryImpl
import com.digitaldude.docshield.data.repository.ScanRepositoryImpl
import com.digitaldude.docshield.domain.repository.DocumentRepository
import com.digitaldude.docshield.domain.repository.ScanRepository
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.ExtractTextUseCase
import com.digitaldude.docshield.domain.usecase.GetDocumentsUseCase
import com.digitaldude.docshield.presentation.viewmodel.AuthViewModel
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import com.digitaldude.docshield.presentation.viewmodel.ScanViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DocumentRepository> { DocumentRepositoryImpl(get<DocShieldDatabase>().documentDao()) }
    factory { GetDocumentsUseCase(get()) }
    factory { AddDocumentUseCase(get()) }

    viewModel { DocumentViewModel(get(), get()) }
    viewModel{ AuthViewModel(get()) }
    viewModel{ ScanViewModel(get(), get(), get()) }


    //Scanner
    single{ DocumentScannerDataSource(get()) }
    single{ TextRecognitionDataSource(get()) }
    single<ScanRepository>{ ScanRepositoryImpl(get()) }
    factory { ExtractTextUseCase(get()) }

    single{ DatabaseKeyManager(get()) }
    single{ DocShieldDatabase.create(androidContext(), get<DatabaseKeyManager>().getOrCreatePassphrase()) }


    single{ BiometricAuthManager(get()) }
    single { GeminiNanoDataSource(androidContext()) }

}