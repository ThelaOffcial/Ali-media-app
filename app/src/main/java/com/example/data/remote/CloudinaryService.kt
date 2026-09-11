package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Cloudinary unsigned upload – same credentials as the live site.
 * Cloud name: drmmn0xp3
 * Upload preset: alimanagement
 * Folder: alimedia_uploads
 */
object CloudinaryService {

    private const val TAG = "CloudinaryService"
    const val CLOUD_NAME = "drmmn0xp3"
    const val UPLOAD_PRESET = "alimanagement"
    private const val FOLDER = "alimedia_uploads"
    private val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    data class UploadResult(val url: String, val publicId: String)

    /**
     * Upload an image file to Cloudinary. Returns secure_url + public_id.
     * If [imageSource] is already an http(s) URL, returns it unchanged.
     */
    suspend fun uploadPhoto(imageSource: String): UploadResult = withContext(Dispatchers.IO) {
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            return@withContext UploadResult(url = imageSource, publicId = "")
        }
        throw IllegalArgumentException("Pass a local file path or already-hosted URL")
    }

    suspend fun uploadFile(file: File): UploadResult = withContext(Dispatchers.IO) {
        require(file.exists() && file.length() > 0) { "File missing or empty" }

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                RequestBody.create(MediaType.parse("image/*"), file)
            )
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("folder", FOLDER)
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e(TAG, "Upload failed HTTP ${response.code}: $raw")
                throw RuntimeException("Cloudinary error (${response.code}): $raw")
            }
            val json = JSONObject(raw)
            val url = json.optString("secure_url").ifBlank { json.optString("url") }
            val publicId = json.optString("public_id")
            if (url.isBlank() || url.startsWith("data:")) {
                throw RuntimeException("Cloudinary did not return a valid hosted URL")
            }
            Log.i(TAG, "Uploaded → $url")
            UploadResult(url = url, publicId = publicId)
        }
    }

    /** Upload raw JPEG/PNG bytes (e.g. from camera / gallery picker). */
    suspend fun uploadBytes(bytes: ByteArray, filename: String = "upload.jpg"): UploadResult =
        withContext(Dispatchers.IO) {
            require(bytes.isNotEmpty()) { "Empty image bytes" }

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    filename,
                    RequestBody.create(MediaType.parse("image/*"), bytes)
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", FOLDER)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Upload failed HTTP ${response.code}: $raw")
                    throw RuntimeException("Cloudinary error (${response.code}): $raw")
                }
                val json = JSONObject(raw)
                val url = json.optString("secure_url").ifBlank { json.optString("url") }
                val publicId = json.optString("public_id")
                if (url.isBlank()) throw RuntimeException("Cloudinary missing secure_url")
                UploadResult(url = url, publicId = publicId)
            }
        }
}
