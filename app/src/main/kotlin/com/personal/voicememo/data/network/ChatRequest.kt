package com.personal.voicememo.data.network

/**
 * Request structure for chat completion API calls.
 * 
 * @property model The model to use for completion generation (default: "gpt-3.5-turbo")
 * @property messages List of messages in the conversation
 */
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

/**
 * Structure representing a message in a chat conversation.
 * 
 * @property role The role of the message sender (e.g., "system", "user", "assistant")
 * @property content The content of the message
 */
data class Message(
    val role: String,
    val content: String
)

/**
 * Response structure for chat completion API calls.
 * 
 * @property id Unique identifier for the completion
 * @property object The type of object returned
 * @property created Timestamp of creation
 * @property model The model used for generation
 * @property choices List of generated completions
 * @property usage Token usage statistics
 */
data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

/**
 * Structure representing a single completion choice.
 * 
 * @property index Index of the choice
 * @property message The generated message
 * @property finish_reason The reason why the generation finished
 */
data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

/**
 * Structure representing token usage statistics.
 * 
 * @property prompt_tokens Number of tokens in the prompt
 * @property completion_tokens Number of tokens in the completion
 * @property total_tokens Total number of tokens used
 */
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
) 