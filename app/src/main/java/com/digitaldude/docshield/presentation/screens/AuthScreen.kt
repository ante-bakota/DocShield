package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.digitaldude.docshield.presentation.components.FullScreenLoader
import com.digitaldude.docshield.presentation.components.SecureButton
import com.digitaldude.docshield.presentation.components.VaultBackground
import com.digitaldude.docshield.presentation.components.VaultCard
import com.digitaldude.docshield.presentation.viewmodel.AuthState
import com.digitaldude.docshield.presentation.viewmodel.AuthViewModel
import com.digitaldude.docshield.ui.theme.DocAmber
import com.digitaldude.docshield.ui.theme.DocLavender
import com.digitaldude.docshield.ui.theme.DocTeal

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.AuthScreen.route) { inclusive = true }
            }
        }
    }

    when (authState) {
        is AuthState.Authenticating,
        is AuthState.Authenticated -> {
            FullScreenLoader(message = "Unlocking DocShield...")
        }

        else -> {
            VaultBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VaultCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(DocLavender, DocTeal, DocAmber)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "DS",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DocShield",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Your private document vault",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (authState is AuthState.Error) {
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            SecureButton(
                                onClick = {
                                    viewModel.authenticate(context as FragmentActivity)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (authState is AuthState.Error) "Try again" else "Unlock DocShield")
                            }
                        }
                    }
                }
            }
        }
    }
}
