package com.digitaldude.docshield.di

import com.digitaldude.docshield.data.repository.DocumentRepositoryImpl
import com.digitaldude.docshield.domain.repository.DocumentRepository
import com.digitaldude.docshield.domain.usecase.AddDocumentUseCase
import com.digitaldude.docshield.domain.usecase.GetDocumentsUseCase
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DocumentRepository> { DocumentRepositoryImpl() }
    factory { GetDocumentsUseCase(get()) }
    factory { AddDocumentUseCase(get()) }

    viewModel { DocumentViewModel(get(), get()) }
}