package com.bit.bilikdigitalkarawang.utils

import android.util.Base64
import android.util.Log
import com.bit.bilikdigitalkarawang.common.Constant
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Encrypt {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    // ========== ENCRYPT FUNCTIONS ==========

    /**
     * Encrypt String - handle null dengan return empty string
     */
    fun encrypt(plainText: String?): String {
        Log.d("EncryptAlddy", "Input: $plainText")

        if (plainText.isNullOrEmpty()) {
            Log.d("EncryptAlddy", "Input is null or empty")
            return ""
        }

        try {
            val secretKey = Constant.SECRET_KEY
            Log.d("EncryptAlddy", "Secret key length: ${secretKey.length}")

            // Convert key dari Base64 string ke SecretKey
            val keyBytes = Base64.decode(secretKey, Base64.NO_WRAP)
            val key: SecretKey = SecretKeySpec(keyBytes, "AES")

            // Generate random IV (Initialization Vector)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // Setup cipher
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec)

            // Encrypt data
            val encryptedData = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Gabungkan IV + Encrypted Data
            val combined = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

            // Return as Base64 string
            val result = Base64.encodeToString(combined, Base64.NO_WRAP)
            Log.d("EncryptAlddy", "Encrypted successfully, length: ${result.length}")
            return result

        } catch (e: Exception) {
            Log.e("Encrypt", "Encryption failed", e)
            e.printStackTrace()
            return ""
        }
    }

    /**
     * Encrypt Integer - convert ke String dulu
     */
    fun encrypt(number: Int?): String {
        if (number == null) {
            Log.d("EncryptAlddy", "Int is null")
            return ""
        }
        Log.d("EncryptAlddy", "Encrypting Int: $number")
        return encrypt(number.toString())
    }


    // ========== DECRYPT FUNCTIONS ==========

    /**
     * Decrypt ke String - return null jika empty atau gagal
     */
    fun decryptToString(encryptedText: String?): String? {
        if (encryptedText.isNullOrEmpty()) return null

        try {
            val secretKey = Constant.SECRET_KEY
            // Convert key dari Base64 string ke SecretKey
            val keyBytes = Base64.decode(secretKey, Base64.NO_WRAP)
            val key: SecretKey = SecretKeySpec(keyBytes, "AES")

            // Decode encrypted data
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // Extract IV dan encrypted data
            val iv = ByteArray(GCM_IV_LENGTH)
            val encryptedData = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, encryptedData, 0, encryptedData.size)

            // Setup cipher
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)

            // Decrypt data
            val decryptedData = cipher.doFinal(encryptedData)

            return String(decryptedData, Charsets.UTF_8)

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Decrypt ke String dengan default value jika null
     */
    fun decryptToStringOrEmpty(encryptedText: String?): String {
        return decryptToString(encryptedText) ?: ""
    }

    /**
     * Decrypt ke Integer - return null jika empty atau gagal convert
     */
    fun decryptToInt(encryptedText: String?): Int? {
        val decrypted = decryptToString(encryptedText)
        return decrypted?.toIntOrNull()
    }

    /**
     * Decrypt ke Integer dengan default value
     */
    fun decryptToIntOrDefault(encryptedText: String?, defaultValue: Int = 0): Int {
        return decryptToInt(encryptedText) ?: defaultValue
    }

    /**
     * Decrypt original (backward compatibility)
     * Throw exception jika gagal
     */
    fun decrypt(encryptedText: String): String {
        return decryptToString(encryptedText)
            ?: throw Exception("Decryption failed: empty or invalid data")
    }
}