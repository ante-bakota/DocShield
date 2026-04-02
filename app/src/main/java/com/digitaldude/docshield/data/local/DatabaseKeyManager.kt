package com.digitaldude.docshield.data.local
import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom


class DatabaseKeyManager(private val context: Context) {
    companion object{
        private const val PREFS_FILE = "docshield_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val KEY_SIZE_BYTES = 32
    }

    fun getOrCreatePassphrase() : ByteArray{
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // EncryptedSharedPreferences needs a MasterKey to encrypt/decrypt its contents.
        // This key lives in Android Keystore (hardware-backed) —we never touch it directly.
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,       // name of the encrypted prefs file on disk
            masterKey,        // used internally to protect the prefs file
            // AES256_SIV for keys: deterministic encryption — needed so lookups by key name work
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            // AES256_GCM for values: authenticated encryption — protects the actual db passphrase
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val stored = prefs.getString(KEY_DB_PASSPHRASE, null)

        //If passphrase already exists, decode it from BASE 64 and return
        return if(stored != null) {
            Base64.decode(stored, Base64.DEFAULT)
        }else{
            // First install — generate 32 cryptographically random bytes
            val newKey = ByteArray(KEY_SIZE_BYTES)
            SecureRandom().nextBytes(newKey)

            // Store as Base64 string (SharedPreferences can only store Strings, not ByteArrays)
            prefs.edit()
                .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(newKey, Base64.DEFAULT))
                .apply()

        newKey
        }
    }

}