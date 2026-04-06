package com.digitaldude.docshield.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.digitaldude.docshield.presentation.viewmodel.AuthState
import com.digitaldude.docshield.presentation.viewmodel.AuthViewModel
import com.digitaldude.docshield.presentation.viewmodel.DocumentViewModel

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // LaunchedEffect — executed when auth state changes
// is state is "Authenticated"  navigate to home screen
    LaunchedEffect(authState){
        if (authState is AuthState.Authenticated){
            navController.navigate(Screen.Home.route){
                popUpTo(Screen.AuthScreen.route){inclusive = true}
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "DocShield",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Text(text = "Your private document vault")

            when (authState){
                is AuthState.Idle -> {
                    Button(onClick = {
                        viewModel.authenticate(context as FragmentActivity)
                    }) {
                        Text("Unlock DocShield")
                    }
                }
                is AuthState.Authenticating -> {
                    CircularProgressIndicator()
                }
                is AuthState.Authenticated -> {
                    //Launched effect will take care of navigation
                    CircularProgressIndicator()
                }
                is AuthState.Error -> {
                    val message = (authState  as AuthState.Error).message

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = {
                        viewModel.authenticate(context as FragmentActivity)
                    }) {
                        Text("Try again")
                    }
                }
            }
        }
    }

}