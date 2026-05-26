package ai.atavus.sdk.ui

import ai.atavus.sdk.AtavusClient
import ai.atavus.sdk.Session
import ai.atavus.sdk.models.AssistantResponse
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Data class representing a single chat message in the UI.
 */
data class ChatUIMessage(
    val id: String,
    val text: String,
    val isUser: Boolean
)

/**
 * A full chat screen that connects to an Atavus AI assistant.
 *
 * @param client The initialized [AtavusClient].
 * @param theme The visual theme for the chat UI.
 */
@Composable
fun AtavusChatScreen(
    client: AtavusClient,
    theme: AtavusChatTheme = AtavusChatTheme.Default,
    onClose: (() -> Unit)? = null
) {
    var messages by remember { mutableStateOf(listOf<ChatUIMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var session by remember { mutableStateOf<Session?>(null) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var messageCounter by remember { mutableStateOf(0) }

    // Initialize session
    LaunchedEffect(Unit) {
        try {
            session = client.createSession()
        } catch (e: Exception) {
            errorMessage = "Connection failed: ${e.message}"
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🤖 Atavus AI",
                color = theme.primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            onClose?.let {
                TextButton(onClick = it) {
                    Text("✕", color = Color.Gray, fontSize = 20.sp)
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // Messages area
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty() && !isLoading) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✨",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ask me anything!",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(
                        text = message.text,
                        isUser = message.isUser,
                        theme = theme
                    )
                }

                if (isLoading) {
                    item {
                        ThinkingIndicator()
                    }
                }

                errorMessage?.let { error ->
                    item {
                        ErrorBanner(message = error)
                    }
                }
            }
        }

        // Input area
        Column {
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Type a message...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 120.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = theme.primaryColor,
                        focusedTextColor = theme.textColor,
                        unfocusedTextColor = theme.textColor,
                        focusedContainerColor = theme.inputBackgroundColor,
                        unfocusedContainerColor = theme.inputBackgroundColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isLoading) {
                                scope.launch {
                                    sendMessage(
                                        text = inputText.trim(),
                                        client = client,
                                        session = session,
                                        onMessageAdded = { msg ->
                                            messages = messages + msg
                                            messageCounter++
                                        },
                                        onLoading = { isLoading = it },
                                        onError = { errorMessage = it },
                                        onNewMessageId = { messageCounter++ }
                                    )
                                }
                                inputText = ""
                            }
                        }
                    ),
                    singleLine = false,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        val trimmed = inputText.trim()
                        if (trimmed.isNotBlank() && !isLoading) {
                            scope.launch {
                                sendMessage(
                                    text = trimmed,
                                    client = client,
                                    session = session,
                                    onMessageAdded = { msg ->
                                        messages = messages + msg
                                        messageCounter++
                                    },
                                    onLoading = { isLoading = it },
                                    onError = { errorMessage = it },
                                    onNewMessageId = { messageCounter++ }
                                )
                            }
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (inputText.isNotBlank()) theme.primaryColor
                        else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text("↑", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isUser: Boolean,
    theme: AtavusChatTheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) theme.userBubbleColor else theme.bubbleBackgroundColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) theme.userTextColor else theme.bubbleTextColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFF9800).copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFFFF9800),
                fontSize = 12.sp
            )
        }
    }
}

private suspend fun sendMessage(
    text: String,
    client: AtavusClient,
    session: Session?,
    onMessageAdded: (ChatUIMessage) -> Unit,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onNewMessageId: () -> Int
) {
    onError(null)
    onLoading(true)
    onMessageAdded(ChatUIMessage(id = "msg-${onNewMessageId()}", text = text, isUser = true))

    try {
        val response = if (session != null) {
            session.send(text)
        } else {
            client.sendMessage(text)
        }
        onMessageAdded(
            ChatUIMessage(
                id = "msg-${onNewMessageId()}",
                text = response.text,
                isUser = false
            )
        )
    } catch (e: Exception) {
        onError(e.message ?: "An error occurred")
    }
    onLoading(false)
}
