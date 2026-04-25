package ai.atavus.sdk.internal

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

/** Internal parser for Server-Sent Events (SSE). */
internal object SSEParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Parses raw SSE data and returns parsed events.
     * Handles field ordering: event, data, id.
     */
    fun parseEvents(data: String): List<ParsedEvent> {
        val events = mutableListOf<ParsedEvent>()
        val lines = data.split("\n")
        var currentType: String? = null
        var currentData = StringBuilder()
        var currentId: String? = null

        for (line in lines) {
            when {
                line.startsWith("event:") -> {
                    currentType = line.removePrefix("event:").trim()
                }
                line.startsWith("data:") -> {
                    val value = line.removePrefix("data:").trim()
                    if (currentData.isNotEmpty()) currentData.append("\n")
                    currentData.append(value)
                }
                line.startsWith("id:") -> {
                    currentId = line.removePrefix("id:").trim()
                }
                line.isEmpty() -> {
                    if (currentData.isNotEmpty()) {
                        events.add(
                            ParsedEvent(
                                data = currentData.toString(),
                                type = currentType,
                                id = currentId
                            )
                        )
                    }
                    currentType = null
                    currentData = StringBuilder()
                    currentId = null
                }
            }
        }
        return events
    }

    data class ParsedEvent(
        val data: String,
        val type: String?,
        val id: String?
    )
}
