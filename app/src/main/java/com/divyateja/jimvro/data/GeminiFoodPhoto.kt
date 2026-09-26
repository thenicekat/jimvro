package com.divyateja.jimvro.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

private const val GEMINI_MODEL = "gemini-3.8-flash"
private const val MAX_IMAGE_SIDE = 1_280
private const val JPEG_QUALITY = 85
private const val TRY_AGAIN_MESSAGE =
    "Gemini is busy right now. Please try again in a moment."

internal fun compressFoodPhoto(bytes: ByteArray): ByteArray {
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: error("Could not decode image")
    val longest = max(original.width, original.height).coerceAtLeast(1)
    val scaled = if (longest > MAX_IMAGE_SIDE) {
        val factor = MAX_IMAGE_SIDE.toFloat() / longest
        Bitmap.createScaledBitmap(
            original,
            (original.width * factor).roundToInt().coerceAtLeast(1),
            (original.height * factor).roundToInt().coerceAtLeast(1),
            true,
        ).also { if (it !== original) original.recycle() }
    } else {
        original
    }
    return try {
        ByteArrayOutputStream().use { out ->
            require(scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                "Could not compress image"
            }
            out.toByteArray()
        }
    } finally {
        scaled.recycle()
    }
}

internal fun geminiUserMessage(apiMessage: String, code: Int): String {
    if (code == 429 || code == 503) return TRY_AGAIN_MESSAGE
    val lower = apiMessage.lowercase()
    return if (
        "high demand" in lower ||
        "try again later" in lower ||
        "resource exhausted" in lower ||
        "unavailable" in lower ||
        "overloaded" in lower
    ) {
        TRY_AGAIN_MESSAGE
    } else {
        apiMessage
    }
}

suspend fun requestFoodPhotoEstimate(
    apiKey: String,
    imageBytes: ByteArray,
    mimeType: String = "image/jpeg",
): Result<FoodPhotoEstimate> = withContext(Dispatchers.IO) {
    runCatching {
        val key = apiKey.trim()
        require(key.isNotBlank()) { "Add a Gemini API key in Settings first" }
        val jpeg = compressFoodPhoto(imageBytes)
        val (code, body) = postGemini(GEMINI_MODEL, key, buildGeminiFoodPayload(jpeg))
        if (code !in 200..299) {
            val apiMessage = runCatching {
                JSONObject(body).getJSONObject("error").getString("message")
            }.getOrNull() ?: "Gemini request failed ($code)"
            error(geminiUserMessage(apiMessage, code))
        }
        val text = JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        parseFoodPhotoEstimate(text)
    }
}

private fun buildGeminiFoodPayload(jpeg: ByteArray): JSONObject =
    JSONObject()
        .put(
            "contents",
            org.json.JSONArray().put(
                JSONObject().put(
                    "parts",
                    org.json.JSONArray()
                        .put(
                            JSONObject().put(
                                "inline_data",
                                JSONObject()
                                    .put("mime_type", "image/jpeg")
                                    .put("data", Base64.encodeToString(jpeg, Base64.NO_WRAP)),
                            ),
                        )
                        .put(
                            JSONObject().put(
                                "text",
                                """
                                Estimate nutrition for the food shown in this photo for one typical plated serving.
                                Respond with JSON only using keys name, calories, proteinG, carbsG, fatG.
                                name is a short food label. Macro values are numbers for the whole serving shown.
                                If several items appear, treat them as one meal.
                                """.trimIndent(),
                            ),
                        ),
                ),
            ),
        )
        .put(
            "generationConfig",
            JSONObject()
                .put("temperature", 0.2)
                .put("responseMimeType", "application/json")
                .put(
                    "responseSchema",
                    JSONObject()
                        .put("type", "OBJECT")
                        .put(
                            "properties",
                            JSONObject()
                                .put("name", JSONObject().put("type", "STRING"))
                                .put("calories", JSONObject().put("type", "NUMBER"))
                                .put("proteinG", JSONObject().put("type", "NUMBER"))
                                .put("carbsG", JSONObject().put("type", "NUMBER"))
                                .put("fatG", JSONObject().put("type", "NUMBER")),
                        )
                        .put(
                            "required",
                            org.json.JSONArray(listOf("name", "calories", "proteinG", "carbsG", "fatG")),
                        ),
                ),
        )

private fun postGemini(model: String, apiKey: String, payload: JSONObject): Pair<Int, String> {
    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.connectTimeout = 20_000
    connection.readTimeout = 45_000
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("X-goog-api-key", apiKey)
    connection.setRequestProperty("User-Agent", "jimvro-android/0.1 (local-first food diary)")
    return try {
        connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }
        val code = connection.responseCode
        val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()
        code to body
    } finally {
        connection.disconnect()
    }
}
