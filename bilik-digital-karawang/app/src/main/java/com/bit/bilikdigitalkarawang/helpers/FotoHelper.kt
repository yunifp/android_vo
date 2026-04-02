package com.bit.bilikdigitalkarawang.helpers

import android.content.Context
import android.util.Log
import com.bit.bilikdigitalkarawang.common.Constant
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

object FotoHelper {
    fun saveImageToInternalStorage(context: Context, imageUrl: String): String {
        return try {
            Log.d(Constant.LOG_TAG, "Attempting to download image from: $imageUrl")

            val fileName = "foto_kandidat_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)

            val input = URL(imageUrl).openStream()
            val output = FileOutputStream(file)

            input.copyTo(output)
            input.close()
            output.close()

            Log.d(Constant.LOG_TAG, "Image saved to ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Constant.LOG_TAG, "Failed to save image: ${e.message}", e)
            ""
        }
    }

}