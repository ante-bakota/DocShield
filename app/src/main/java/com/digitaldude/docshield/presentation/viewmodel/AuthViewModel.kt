package com.digitaldude.docshield.presentation.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digitaldude.docshield.data.local.BiometricAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState{
    object Idle : AuthState()
    object Authenticating : AuthState()
    object Authenticated : AuthState()
    data class Error(val message : String) : AuthState()
}
@HiltViewModel
class AuthViewModel @Inject constructor(private val biometricAuthManager: BiometricAuthManager) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState : StateFlow<AuthState> = _authState.asStateFlow()

    init {
        collectAuthResults()
    }

    fun authenticate(activity :  FragmentActivity){
        _authState.value = AuthState.Authenticating
        biometricAuthManager.authenticate(activity)
    }

    private fun collectAuthResults(){
        viewModelScope.launch {
            biometricAuthManager.authResult.collect{ result ->
                _authState.value = when (result){
                    is BiometricResult.Success -> AuthState.Authenticated
                    is BiometricResult.Failed -> AuthState.Idle
                    is BiometricResult.Error -> AuthState.Error(result.message)
                    is BiometricResult.HardwareUnavailable -> AuthState.Error("Biometry is unavailable.")
                    is BiometricResult.NoBiometricEnrolled -> AuthState.Error("No registered fingerprint.")
                }
            }
        }
    }
}

