package ai.atavus.sdk

/**
 * Errors that can occur when using the Atavus AI SDK.
 */
sealed class AtavusError : Exception {
    /** Invalid API key or authentication failure. */
    data class Authentication(val msg: String) : AtavusError()
    /** Network connectivity issue. */
    data class Network(val cause: Throwable) : AtavusError()
    /** Server returned an error. */
    data class Server(val statusCode: Int, val msg: String) : AtavusError()
    /** Failed to decode the response. */
    data class Decoding(val cause: Throwable) : AtavusError()
    /** Rate limit exceeded. */
    data class RateLimit(val retryAfter: Long) : AtavusError()
    /** Invalid request parameters. */
    data class InvalidRequest(val msg: String) : AtavusError()
    /** SDK not initialized. */
    data object NotInitialized : AtavusError()
    /** Session error. */
    data class Session(val msg: String) : AtavusError()
    /** Streaming error. */
    data class Stream(val msg: String) : AtavusError()
    /** Unknown error. */
    data class Unknown(val msg: String) : AtavusError()

    override val message: String get() = when (this) {
        is Authentication -> "Authentication failed: $msg"
        is Network -> "Network error: ${cause.message}"
        is Server -> "Server error ($statusCode): $msg"
        is Decoding -> "Decoding error: ${cause.message}"
        is RateLimit -> "Rate limited. Retry after ${retryAfter}ms"
        is InvalidRequest -> "Invalid request: $msg"
        is NotInitialized -> "AtavusClient not initialized"
        is Session -> "Session error: $msg"
        is Stream -> "Stream error: $msg"
        is Unknown -> "Unknown error: $msg"
    }
}
