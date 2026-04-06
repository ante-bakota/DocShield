sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult() // example wrong finger, you can try again
    data class Error(val message : String) : BiometricResult() // fatal error
    object HardwareUnavailable : BiometricResult()
    object NoBiometricEnrolled : BiometricResult()
}
