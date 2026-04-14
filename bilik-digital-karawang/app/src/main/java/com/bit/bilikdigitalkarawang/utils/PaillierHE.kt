package com.bit.bilikdigitalkarawang.utils

import java.math.BigInteger
import java.security.SecureRandom
import com.google.gson.Gson

object PaillierHE {
    private const val BIT_LENGTH = 512
    private val random = SecureRandom()

    var n: BigInteger? = null
    private var nSquare: BigInteger? = null
    private var g: BigInteger? = null
    private var lambda: BigInteger? = null
    private var mu: BigInteger? = null

    // Fungsi untuk menghasilkan key baru JIKA BELUM ADA
    fun generateKeys() {
        if (n != null) return
        val p = BigInteger(BIT_LENGTH / 2, 64, random)
        val q = BigInteger(BIT_LENGTH / 2, 64, random)

        n = p.multiply(q)
        nSquare = n!!.multiply(n!!)
        g = n!!.add(BigInteger.ONE)

        val pMinusOne = p.subtract(BigInteger.ONE)
        val qMinusOne = q.subtract(BigInteger.ONE)

        lambda = pMinusOne.multiply(qMinusOne).divide(pMinusOne.gcd(qMinusOne))
        val l = g!!.modPow(lambda, nSquare).subtract(BigInteger.ONE).divide(n!!)
        mu = l.modInverse(n!!)
    }

    // ==== TAMBAHAN BARU UNTUK BACKUP & PERSISTENCE ====
    data class PaillierKeySet(
        val nStr: String,
        val nSquareStr: String,
        val gStr: String,
        val lambdaStr: String,
        val muStr: String
    )

    // Export Kunci untuk disimpan di LocalDB/MicroSD
    fun exportKeysToJson(): String {
        generateKeys() // Pastikan key ada sebelum di-export
        val keySet = PaillierKeySet(
            nStr = n.toString(),
            nSquareStr = nSquare.toString(),
            gStr = g.toString(),
            lambdaStr = lambda.toString(),
            muStr = mu.toString()
        )
        return Gson().toJson(keySet)
    }

    // Import Kunci dari LocalDB/MicroSD (Agar bisa dekripsi file lama)
    fun importKeysFromJson(jsonString: String) {
        try {
            val keySet = Gson().fromJson(jsonString, PaillierKeySet::class.java)
            n = BigInteger(keySet.nStr)
            nSquare = BigInteger(keySet.nSquareStr)
            g = BigInteger(keySet.gStr)
            lambda = BigInteger(keySet.lambdaStr)
            mu = BigInteger(keySet.muStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // ===================================================

    fun encrypt(m: Int): String {
        if (n == null) generateKeys()
        val mBig = BigInteger.valueOf(m.toLong())
        var r: BigInteger
        do {
            r = BigInteger(BIT_LENGTH, random)
        } while (r >= n!! || r <= BigInteger.ZERO || r.gcd(n!!) != BigInteger.ONE)

        val gM = g!!.modPow(mBig, nSquare)
        val rN = r.modPow(n, nSquare)
        return gM.multiply(rN).mod(nSquare).toString()
    }

    fun decrypt(cipherText: String): Int {
        if (lambda == null || mu == null) throw IllegalStateException("Kunci Private Hilang! Harap Import Kunci.")
        val c = BigInteger(cipherText)
        val u = c.modPow(lambda, nSquare).subtract(BigInteger.ONE).divide(n!!)
        return u.multiply(mu).mod(n!!).toInt()
    }

    fun addEncryptedVotes(cipher1: String, cipher2: String): String {
        val c1 = BigInteger(cipher1)
        val c2 = BigInteger(cipher2)
        return c1.multiply(c2).mod(nSquare).toString()
    }
}