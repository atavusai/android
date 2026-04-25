# Atavus AI — Android SDK

Integrate Atavus AI assistants into your Android app with native Kotlin.

## Requirements

- Android API 26+ (Android 8.0+)
- Kotlin 1.9+
- Jetpack Compose (for UI components)

## Installation

### Option 1: Module Import (Recommended)

1. Download the SDK: [atavus-android-sdk-1.0.0.tar.gz](https://atavus.ai/sdk/atavus-android-sdk-1.0.0.tar.gz)
2. Extract to your project root
3. Add to `settings.gradle.kts`:
```kotlin
include(":atavus-sdk")
```
4. Add to your app's `build.gradle.kts`:
```kotlin
implementation(project(":atavus-sdk"))
```

### Option 2: Local Maven

Publish to a local Maven repo and add as a dependency.

## Quick Start

```kotlin
// Initialize the client
val client = AtavusClient(
    apiKey = "atavus_sk_your_key_here",
    assistantId = "ast_your_assistant_id"
)

// Send a message (coroutine scope required)
lifecycleScope.launch {
    val response = client.sendMessage("What's the weather?")
    println(response.text)
}

// Stream response
val flow = client.streamMessage("Write a poem")
lifecycleScope.launch {
    flow.collect { chunk ->
        print(chunk.text)
    }
}
```

## Sessions (Conversation Context)

```kotlin
val session = client.createSession()

// Messages maintain conversation history
session.send("My name is Alice")
val reply = session.send("What's my name?")
// reply.text == "Your name is Alice!"
```

## Chat Widget (Jetpack Compose)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = AtavusClient(
            apiKey = "atavus_sk_...",
            assistantId = "ast_..."
        )

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                // Your app content

                AtavusChatButton(
                    client = client,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}
```

## Configuration

```kotlin
val config = AtavusConfig(
    baseURL = "https://atavus.ai/api/v1",
    timeout = 60_000L,
    logLevel = AtavusConfig.LogLevel.DEBUG
)
val client = AtavusClient(apiKey = "...", assistantId = "...", config = config)
```

## Error Handling

```kotlin
try {
    val response = client.sendMessage("Hello")
} catch (e: AtavusError.Authentication) {
    // Invalid API key
} catch (e: AtavusError.RateLimit) {
    // Too many requests — wait retryAfter ms
} catch (e: AtavusError.Network) {
    // No internet connection
}
```
