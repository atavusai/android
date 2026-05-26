package ai.atavus.sdk

/**
 * Configuration for the Atavus AI SDK client.
 */
data class AtavusConfig(
    /** Base URL for the Atavus API. */
    val baseURL: String = "https://atavus.ai/api/v1",
    /** Request timeout in milliseconds (default: 30,000ms = 30 seconds). */
    val timeout: Long = 30_000L,
    /** Logging level. */
    val logLevel: LogLevel = LogLevel.ERROR
) {
    enum class LogLevel { NONE, ERROR, INFO, DEBUG }
}
