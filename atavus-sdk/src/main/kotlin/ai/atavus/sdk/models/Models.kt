package ai.atavus.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A message in a conversation. */
@Serializable
data class Message(
    val role: String,
    val content: String,
    val timestamp: String? = null
)

/** Response from the Atavus AI API. */
@Serializable
data class AssistantResponse(
    @SerialName("response") val text: String,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
    val usage: TokenUsage? = null
)

/** Token usage information. */
@Serializable
data class TokenUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)

/** A chunk of a streaming response. */
@Serializable
data class StreamChunk(
    val text: String = "",
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
    val error: String? = null
)

/** A conversation session. */
@Serializable
data class ChatSessionData(
    val id: String,
    @SerialName("assistant_id") val assistantId: String,
    val messages: List<Message> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)

/** Request body for sending a message. */
@Serializable
data class ChatRequest(
    val message: String,
    @SerialName("session_id") val sessionId: String? = null,
    val stream: Boolean = false
)

/** Health check response. */
@Serializable
data class HealthResponse(
    val status: String
)
