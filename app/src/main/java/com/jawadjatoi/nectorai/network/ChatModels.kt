package com.jawadjatoi.nectorai.network

data class ChatRequest(
    val model: String = "deepseek/deepseek-r1-distill-llama-70b:free",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class ChatMessage(
    val content: String,
    val imageUrl: String? = null,
    val isUserMessage: Boolean = false,
    val isTyping: Boolean = false,
    val isImage: Boolean = false
)

data class TextToImageRequest(
    val inputs: String
)
