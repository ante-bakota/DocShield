package com.digitaldude.docshield.presentation.screens

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{documentId}"){
        fun createRoute(documentId: Long) = "detail/$documentId"
    }

    object ScanScreen : Screen("scanScreen")

    object AuthScreen : Screen("authScreen")

    object ImportPdfScreen : Screen("importPdf")
    object Category : Screen("category/{category}") {
        fun createRoute(category: String) = "category/$category"
    }
}