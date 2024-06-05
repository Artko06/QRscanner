package com.example.qr_scaner

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

object HttpClient {
    private val client = OkHttpClient()

    fun send(context: Context, url: String, imageName: String): File? {
        val safeImageName = imageName.replace(Regex("[^A-Za-z0-9]"), "")
        val request = Request.Builder()
            .url("$url/get_image/$safeImageName.png")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val file = File(context.getExternalFilesDir(null), "$safeImageName.png")
                val fos = FileOutputStream(file)

                fos.use {
                    response.body?.byteStream()?.copyTo(it)
                }

                file
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}








