package com.digitaldude.docshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.digitaldude.docshield.presentation.screens.DetailScreen
import com.digitaldude.docshield.presentation.screens.HomeScreen
import com.digitaldude.docshield.presentation.screens.Screen
import com.digitaldude.docshield.ui.theme.DocShieldTheme
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.presentation.screens.ScanScreen

class MainActivity : ComponentActivity() {
    private lateinit var documentScannerDataSource: DocumentScannerDataSource


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentScannerDataSource = DocumentScannerDataSource(this)

        enableEdgeToEdge()
        setContent {
            DocShieldTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToDetail = { documentId ->
                                navController.navigate(Screen.Detail.createRoute(documentId))
                            },
                            onNavigateToScan = {
                                navController.navigate(Screen.ScanScreen.route)
                            }
                        )
                    }
                    composable(
                        Screen.Detail.route,
                        arguments = listOf(navArgument("documentId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        DetailScreen(
                            documentId = backStackEntry.arguments?.getLong("documentId") ?: 0L,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        Screen.ScanScreen.route
                    ){
                        ScanScreen(documentScannerDataSource)
                    }
                }
            }
        }
    }
}
