package com.digitaldude.docshield.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.digitaldude.docshield.R
import com.digitaldude.docshield.presentation.components.SecureButton
import com.digitaldude.docshield.presentation.viewmodel.AuthState
import com.digitaldude.docshield.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground
    val authenticate = {
        viewModel.authenticate(context as FragmentActivity)
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.AuthScreen.route) { inclusive = true }
            }
        }
    }

    // Fade in immediately, delay auth so splash exit animation finishes first
    LaunchedEffect(Unit) {
        visible = true
        delay(400)
        authenticate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Upper zone (58%) ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
        ) {
            // Subtle primary-tinted gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                primary.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Logo + app name + tagline
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_docshield_logo),
                        contentDescription = "DocShield",
                        modifier = Modifier.size(88.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "DocShield",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your private vault",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Lower zone: manual recovery if the system prompt is canceled or unavailable.
        if (authState !is AuthState.Authenticated) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (authState) {
                    is AuthState.Error -> {
                        Text(
                            text = (authState as AuthState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    AuthState.Authenticating -> {
                        Text(
                            text = "Waiting for biometric prompt...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onBackground.copy(alpha = 0.55f)
                        )
                    }

                    else -> Spacer(modifier = Modifier.height(4.dp))
                }
                SecureButton(
                    onClick = authenticate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (authState is AuthState.Error) "Try again" else "Unlock DocShield")
                }
            }
        }
    }
}
