package ai.atavus.sdk.ui

import ai.atavus.sdk.AtavusClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A floating chat button that opens the Atavus AI chat screen.
 *
 * ```kotlin
 * Box(modifier = Modifier.fillMaxSize()) {
 *     // Your app content
 *     AtavusChatButton(
 *         client = client,
 *         modifier = Modifier
 *             .align(Alignment.BottomEnd)
 *             .padding(16.dp)
 *     )
 * }
 * ```
 *
 * @param client The initialized [AtavusClient].
 * @param theme The visual theme for the chat UI.
 * @param modifier Modifier for positioning the button.
 */
@Composable
fun AtavusChatButton(
    client: AtavusClient,
    theme: AtavusChatTheme = AtavusChatTheme.Default,
    modifier: Modifier = Modifier
) {
    var showChat by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .shadow(12.dp, CircleShape)
                .clickable { showChat = true },
            shape = CircleShape,
            color = theme.primaryColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "💬",
                    fontSize = 22.sp
                )
            }
        }
    }

    if (showChat) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showChat = false },
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            confirmButton = {},
            dismissButton = {},
            title = null,
            text = {
                AtavusChatScreen(
                    client = client,
                    theme = theme,
                    onClose = { showChat = false }
                )
            },
            containerColor = theme.backgroundColor,
            tonalElevation = 0.dp
        )
    }
}
