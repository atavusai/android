package ai.atavus.sdk.ui

import androidx.compose.ui.graphics.Color

/**
 * A theme configuration for the Atavus AI chat UI components.
 */
data class AtavusChatTheme(
    val primaryColor: Color = Color(0xFF7C3AED), // purple-500
    val secondaryColor: Color = Color(0xFFF59E0B), // amber-500
    val bubbleBackgroundColor: Color = Color.White.copy(alpha = 0.1f),
    val bubbleTextColor: Color = Color.White,
    val userBubbleColor: Color = Color(0xFF7C3AED),
    val userTextColor: Color = Color.White,
    val backgroundColor: Color = Color.Black.copy(alpha = 0.95f),
    val inputBackgroundColor: Color = Color.White.copy(alpha = 0.08f),
    val textColor: Color = Color.White
) {
    companion object {
        val Default = AtavusChatTheme()
    }
}
