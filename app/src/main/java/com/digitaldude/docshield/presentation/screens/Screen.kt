package com.digitaldude.docshield.presentation.screens

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{documentId}"){
        fun createRoute(documentId: Long) = "detail/$documentId"
    }
}