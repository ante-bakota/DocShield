# DocShield

Privacy-first, offline document vault for Android. Zero cloud, zero tracking.

## Features
- Document scanning with automatic crop and perspective correction
- On-device OCR text extraction (no internet required)
- Hardware-backed biometric authentication (Android Keystore)
- AES-256 encrypted local database (SQLCipher)
- AI-powered document categorization (on-device)

## Tech Stack
- Jetpack Compose + MVVM + Clean Architecture
- Kotlin Flow / StateFlow
- Koin Dependency Injection
- CameraX + ML Kit
- BiometricPrompt + Android Keystore
- Room + SQLCipher
- MediaPipe / Gemini Nano (on-device AI)

## Architecture
Clean Architecture with three layers:
- **Presentation** — Compose UI, ViewModels
- **Domain** — Use Cases, Repository interfaces (pure Kotlin)
- **Data** — Room, ML Kit, repository implementations

## Status
In active development