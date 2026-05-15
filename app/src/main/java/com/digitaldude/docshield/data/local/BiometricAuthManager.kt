package com.digitaldude.docshield.data.local

import BiometricResult
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow



class BiometricAuthManager(private val context: Context) {
    private val resultChannel = Channel<BiometricResult>(Channel.CONFLATED)
    val authResult: Flow<BiometricResult> = resultChannel.receiveAsFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun lock() { _isLocked.value = true }
    fun unlock() { _isLocked.value = false }

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(activity: FragmentActivity) {
        val executor = ContextCompat.getMainExecutor(context)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("DocShield")
            .setSubtitle("Verify identity to get access to documents")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()


        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    unlock()
                    resultChannel.trySend(BiometricResult.Success)
                }

                override fun onAuthenticationFailed() {
                    resultChannel.trySend(BiometricResult.Failed)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val result = when (errorCode) {
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometricResult.HardwareUnavailable

                        BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricResult.NoBiometricEnrolled
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricResult.Error("Canceled")

                        else -> BiometricResult.Error(errString.toString())
                    }
                    resultChannel.trySend(result)
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
