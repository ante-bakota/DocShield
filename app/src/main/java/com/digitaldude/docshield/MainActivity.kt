package com.digitaldude.docshield

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digitaldude.docshield.data.local.BiometricAuthManager
import com.digitaldude.docshield.data.ml.DocumentScannerDataSource
import com.digitaldude.docshield.presentation.screens.AuthScreen
import com.digitaldude.docshield.presentation.screens.DetailScreen
import com.digitaldude.docshield.presentation.screens.HomeScreen
import com.digitaldude.docshield.presentation.screens.ImportPdfScreen
import com.digitaldude.docshield.presentation.screens.ScanScreen
import com.digitaldude.docshield.presentation.screens.Screen
import com.digitaldude.docshield.presentation.viewmodel.AuthViewModel
import com.digitaldude.docshield.ui.theme.DocShieldTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private lateinit var documentScannerDataSource: DocumentScannerDataSource
    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    override fun onStop() {
        super.onStop()
        biometricAuthManager.lock()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentScannerDataSource = DocumentScannerDataSource(this)

        enableEdgeToEdge()
        setContent {
            DocShieldTheme {
                val navController = rememberNavController()

                val isLocked by biometricAuthManager.isLocked.collectAsState()
                LaunchedEffect(isLocked) {
                    if (isLocked) {
                        navController.navigate(Screen.AuthScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.AuthScreen.route
                ) {
                    composable(Screen.AuthScreen.route){
                        val authViewModel :  AuthViewModel = hiltViewModel()
                        AuthScreen(
                            navController =  navController,
                            viewModel = authViewModel
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToDetail = { documentId ->
                                navController.navigate(Screen.Detail.createRoute(documentId))
                            },
                            onNavigateToScan = {
                                navController.navigate(Screen.ScanScreen.route)
                            },
                            onNavigateToImportPdf = {
                                navController.navigate(Screen.ImportPdfScreen.route)
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
                        ScanScreen(
                            documentScannerDataSource,
                            onBack = {navController.popBackStack()},
                            onNavigateHome = {
                                navController.popBackStack()

                            }
                            )
                    }
                    composable(Screen.ImportPdfScreen.route){
                        ImportPdfScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
