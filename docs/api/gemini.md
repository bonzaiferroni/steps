# Gemini API

The Gemini API provides endpoints for interacting with the Gemini AI assistant.

## Endpoints

### Chat

**Endpoint:** `/api/v1/gemini/chat`
**Method:** POST
**Request Body:** List of GeminiMessage objects
**Response:** GeminiMessage object (the AI's response)

This endpoint takes a list of messages from the conversation history and returns the AI's response as a new message.

## Data Models

### GeminiMessage

```kotlin
@Serializable
data class GeminiMessage(
    val role: GeminiRole,
    val message: String,
)
```

### GeminiRole

```kotlin
enum class GeminiRole {
    User,
    Assistant
}
```

## Client Implementation

The client uses GeminiStore to interact with the API:

```kotlin
class GeminiStore : ApiStore() {
    suspend fun chat(messages: List<GeminiMessage>): GeminiMessage = 
        client.post(Api.Gemini.Chat, messages)
}
```

## Server Implementation

The server uses GeminiService to process requests:

```kotlin
fun Routing.serveGemini(service: GeminiService = GeminiService()) {
    post(Api.Gemini.Chat) { messages, endpoint ->
        service.chat(messages)
    }
}
```

The GeminiService uses the GeminiClient to generate responses:

```kotlin
suspend fun chat(messages: List<GeminiMessage>): GeminiMessage {
    val formattedMessages = messages.joinToString("\n") { 
        "${it.role}: ${it.message}" 
    }
    
    val response = client.generateText(formattedMessages)
    
    return GeminiMessage(
        role = GeminiRole.Assistant,
        message = response ?: "Arr, the AI be silent! Try again, ye scurvy dog!"
    )
}
```