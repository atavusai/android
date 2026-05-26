package ai.atavus.sdk.internal

import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

/** Internal HTTP client wrapper around OkHttp. */
internal class HttpClient(
    private val baseURL: String,
    private val apiKey: String,
    private val timeout: Long
) {
    private val jsonMediaType = "application/json".toMediaType()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
        .readTimeout(timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(timeout, TimeUnit.MILLISECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** Performs a GET request. */
    suspend fun get(path: String): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val request = buildRequest(path)
            client.newCall(request).execute().use { response ->
                handleResponse(response)
            }
        }
    }

    /** Performs a POST request with a JSON body. */
    suspend fun post(path: String, body: String): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val request = buildRequest(path).newBuilder()
                .post(body.toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { response ->
                handleResponse(response)
            }
        }
    }

    /** Performs an SSE streaming POST request. */
    fun postStream(
        path: String,
        body: String,
        onEvent: (String) -> Unit,
        onError: (AtavusError) -> Unit,
        onComplete: () -> Unit
    ): EventSource {
        val request = buildRequest(path).newBuilder()
            .post(body.toRequestBody(jsonMediaType))
            .build()

        val factory = EventSources.createFactory(client)
        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                onEvent(data)
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                onError(AtavusError.Network(t ?: Exception("SSE connection failed")))
            }

            override fun onClosed(eventSource: EventSource) {
                onComplete()
            }
        }

        return factory.newEventSource(request, listener)
    }

    private fun buildRequest(path: String): Request {
        return Request.Builder()
            .url(baseURL + path)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("User-Agent", "AtavusAI-SDK/Android")
            .addHeader("X-SDK-Version", "1.0.0")
            .build()
    }

    @Throws(AtavusError::class)
    private fun handleResponse(response: Response): String {
        val bodyString = response.body?.string() ?: ""

        when (response.code) {
            in 200..299 -> return bodyString
            401 -> throw AtavusError.Authentication("Invalid or expired API key")
            403 -> throw AtavusError.Authentication("Access forbidden")
            429 -> {
                val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 60_000L
                throw AtavusError.RateLimit(retryAfter)
            }
            else -> {
                val detail = try {
                    json.decodeFromString<Map<String, String>>(bodyString)["detail"]
                } catch (_: Exception) { null }
                throw AtavusError.Server(response.code, detail ?: "Server error")
            }
        }
    }
}
