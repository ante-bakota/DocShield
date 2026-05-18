package com.digitaldude.docshield.di

import android.content.Context
import com.digitaldude.docshield.data.local.BiometricAuthManager
import com.digitaldude.docshield.data.local.ScanSessionHolder
import com.digitaldude.docshield.data.local.DatabaseKeyManager
import com.digitaldude.docshield.data.local.DocShieldDatabase
import com.digitaldude.docshield.data.ml.GeminiNanoDataSource
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseKeyManager(
        @ApplicationContext context: Context
    ): DatabaseKeyManager = DatabaseKeyManager(context)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager
    ): DocShieldDatabase = DocShieldDatabase.create(context, keyManager.getOrCreatePassphrase())

    @Provides
    @Singleton
    fun provideScanSessionHolder(): ScanSessionHolder = ScanSessionHolder()

    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context
    ): BiometricAuthManager = BiometricAuthManager(context)

    @Provides
    @Singleton
    fun provideTextRecognitionDataSource(
        @ApplicationContext context: Context
    ): TextRecognitionDataSource = TextRecognitionDataSource(context)

    @Provides
    @Singleton
    fun providePdfTextExtractionDataSource(
        @ApplicationContext context: Context,
        textRecognition: TextRecognitionDataSource
    ): PdfTextExtractionDataSource = PdfTextExtractionDataSource(context, textRecognition)

    @Provides
    @Singleton
    fun provideLlmInferenceDataSource(
        @ApplicationContext context: Context
    ): LlmInferenceDataSource = LlmInferenceDataSource(context)

    @Provides
    @Singleton
    fun provideGeminiNanoDataSource(
        @ApplicationContext context: Context
    ): GeminiNanoDataSource = GeminiNanoDataSource(context)

    @Provides
    @Singleton
    fun provideRuleBasedCategorizerDataSource(): RuleBasedCategorizerDataSource =
        RuleBasedCategorizerDataSource()

    @Provides
    @Singleton
    fun provideDocumentRepository(
        database: DocShieldDatabase
    ): DocumentRepository = DocumentRepositoryImpl(database.documentDao())

    @Provides
    @Singleton
    fun provideScanRepository(
        textRecognition: TextRecognitionDataSource
    ): ScanRepository = ScanRepositoryImpl(textRecognition)

    @Provides
    @Singleton
    fun provideAiRepository(
        llm: LlmInferenceDataSource,
        gemini: GeminiNanoDataSource,
        ruleBased: RuleBasedCategorizerDataSource
    ): AiRepository = AiRepositoryImpl(llm, gemini, ruleBased)

    @Provides
    fun provideGetDocumentsUseCase(repository: DocumentRepository): GetDocumentsUseCase =
        GetDocumentsUseCase(repository)

    @Provides
    fun provideAddDocumentUseCase(repository: DocumentRepository): AddDocumentUseCase =
        AddDocumentUseCase(repository)

    @Provides
    fun provideExtractTextUseCase(repository: ScanRepository): ExtractTextUseCase =
        ExtractTextUseCase(repository)

    @Provides
    fun provideCategorizeDocumentUseCase(repository: AiRepository): CategorizeDocumentUseCase =
        CategorizeDocumentUseCase(repository)
}