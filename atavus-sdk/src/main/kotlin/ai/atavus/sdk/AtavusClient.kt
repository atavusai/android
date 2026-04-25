package ai.atavus.sdk

import ai.atavus.sdk.internal.HttpClient
import ai.atavus.sdk.internal.SSEParser
import ai.atavus.sdk.models.*
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * The main client for interacting with the Atavus AI API.
 *
 * ```kotlin
 * val client = AtavusClient(
 *     apiKey = "atavus_sk_...",
 *     assistantId = "ast_..."
 * )
 *
 * lifecycleScope.launch {
 *     val response = client.sendMessage("Hello!")
 *     println(response.text)
 * }
 * ```
 */
class AtavusClient(
    val apiKey: String,
    val assistantId: String,
    val config: AtavusConfig = AtavusConfig()
) {
    private val http: HttpClient = HttpClient(config.baseURL, apiKey, config.timeout)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // ── Public API ────────────────────────────────────────

    /**
     * Checks connectivity to the Atavus API.
     * @return true if the API is reachable.
     */
    suspend fun healthCheck(): Boolean {
        log(AtavusConfig.LogLevel.INFO, "healthCheck")
        return http.get("/health").map { body ->
            json.decodeFromString<HealthResponse>(body).status == "ok"
        }.getOrDefault(false)
    }

    /**
     * Sends a message and returns the full response.
     * @param text The message text.
     * @param sessionId Optional session ID for continuing a conversation.
     * @return The assistant's response.
     */
    suspend fun sendMessage(text: String, sessionId: String? = null): AssistantResponse {
        log(AtavusConfig.LogLevel.INFO, "sendMessage: ${text.take(80)}...")

        val request = ChatRequest(message = text, sessionId = sessionId, stream = false)
        val body = json.encodeToString(request)

        return http.post("/chat/$assistantId", body).map { responseBody ->
            json.decodeFromString<AssistantResponse>(responseBody)
        }.getOrThrow()
    }

    /**
     * Sends a message and streams the response in chunks.
     * @param text The message text.
     * @param sessionId Optional session ID for continuing a conversation.
     * @return A flow of response chunks.
     */
    fun streamMessage(text: String, sessionId: String? = null): Flow<StreamChunk> {
        log(AtavusConfig.LogLevel.INFO, "streamMessage: ${text.take(80)}...")

        val request = ChatRequest(message = text, sessionId = sessionId, stream = true)
        val body = json.encodeToString(request)

        return callbackFlow {
            try {
                http.postStream(
                    path = "/chat/$assistantId/stream",
                    body = body,
                    onEvent = { data ->
                        if (data.isNotEmpty()) {
                            try {
                                val chunk = json.decodeFromString<StreamChunk>(data)
                                trySend(chunk)
                                if (chunk.finishReason != null) {
                                    close()
                                }
                            } catch (_: Exception) {
                                // Skip malformed chunks
                            }
                        }
                    },
                    onError = { error ->
                        close(error)
                    },
                    onComplete = {
                        close()
                    }
                )
            } catch (e: Exception) {
                close(AtavusError.Stream(e.message ?: "Stream failed"))
            }

            awaitClose { /* OkHttp SSE handles cleanup */ }
        }
    }

    /**
     * Creates a new conversation session for maintaining context.
     * @return A [Session] object that preserves message history.
     */
    suspend fun createSession(): Session {
        log(AtavusConfig.LogLevel.INFO, "createSession")

        return http.post("/chat/$assistantId/session", "{}").map { body ->
            val data = json.decodeFromString<ChatSessionData>(body)
            Session(client = this, id = data.id, assistantId = assistantId)
        }.getOrThrow()
    }

    // ── Internal ─────────────────────────────────────────

    private fun log(level: AtavusConfig.LogLevel, message: String) {
        if (level.ordinal > config.logLevel.ordinal) return
        val tag = "AtavusAI"
        when (level) {
            AtavusConfig.LogLevel.ERROR -> Log.e(tag, message)
            AtavusConfig.LogLevel.INFO -> Log.i(tag, message)
            AtavusConfig.LogLevel.DEBUG -> Log.d(tag, message)
            AtavusConfig.LogLevel.NONE -> {}
        }
    }
}

/** Extension to convert [Result] to a throwing call. */
private fun <T> Result<T>.getOrThrow(): T = getOrElse { throw it }
