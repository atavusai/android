package ai.atavus.sdk

import ai.atavus.sdk.models.Message
import ai.atavus.sdk.models.StreamChunk
import kotlinx.coroutines.flow.Flow
import ai.atavus.sdk.models.AssistantResponse

/**
 * A conversation session that maintains message history for context.
 *
 * Create via [AtavusClient.createSession], then use [send] and [stream].
 */
class Session internal constructor(
    private val client: AtavusClient,
    val id: String,
    private val assistantId: String
) {
    private val _messages = mutableListOf<Message>()

    /** The message history for this session. */
    val messages: List<Message> get() = _messages.toList()

    /**
     * Sends a message and returns the full response.
     * Both the message and response are appended to conversation history.
     */
    suspend fun send(text: String): AssistantResponse {
        _messages.add(Message(role = "user", content = text))
        val response = client.sendMessage(text, sessionId = id)
        _messages.add(Message(role = "assistant", content = response.text))
        return response
    }

    /**
     * Sends a message and streams the response in chunks.
     * The message is appended immediately; the response is appended on completion.
     */
    fun stream(text: String): Flow<StreamChunk> {
        _messages.add(Message(role = "user", content = text))
        return client.streamMessage(text, sessionId = id)
    }

    /** Clears the conversation history. */
    fun clearHistory() {
        _messages.clear()
    }
}
