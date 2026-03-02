package com.example.coddy

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKeyFactory

/**
 * Provides AES-256 GCM encryption and decryption with PBKDF2 key derivation.
 */
object CryptoEngine {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH_BIT = 256

    /**
     * Encrypts plain text using the given password.
     * Returns a Base64 encoded string containing the salt, IV, and cipher text.
     */
    fun encrypt(plainText: String, password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTE)
        SecureRandom().nextBytes(salt)
        
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        
        val secretKey = getSecretKey(password, salt)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        // Combine Salt + IV + CipherText
        val combined = ByteArray(salt.size + iv.size + cipherText.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(cipherText, 0, combined, salt.size + iv.size, cipherText.size)
        
        return Base64.encodeToString(combined, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    /**
     * Decrypts a Base64 encoded string containing the salt, IV, and cipher text
     * using the given password.
     */
    fun decrypt(encryptedText: String, password: String): String {
        val combined = Base64.decode(encryptedText, Base64.NO_WRAP or Base64.URL_SAFE)
        
        if (combined.size < SALT_LENGTH_BYTE + IV_LENGTH_BYTE) {
            throw IllegalArgumentException("Invalid encrypted payload.")
        }
        
        val salt = ByteArray(SALT_LENGTH_BYTE)
        val iv = ByteArray(IV_LENGTH_BYTE)
        val cipherText = ByteArray(combined.size - SALT_LENGTH_BYTE - IV_LENGTH_BYTE)
        
        System.arraycopy(combined, 0, salt, 0, salt.size)
        System.arraycopy(combined, salt.size, iv, 0, iv.size)
        System.arraycopy(combined, salt.size + iv.size, cipherText, 0, cipherText.size)
        
        val secretKey = getSecretKey(password, salt)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        
        val decryptedBytes = cipher.doFinal(cipherText)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun getSecretKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BIT)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
