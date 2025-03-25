# Voice Memo Android App

An Android application that records voice memos, transcribes them using OpenAI's Whisper API, and stores them in Pinecone's vector database for future semantic search capabilities.

## Features

- 🎙️ Voice memo recording with high-quality audio capture
- 🔄 Real-time transcription using OpenAI Whisper
- 🔍 Vector embeddings for semantic search (using OpenAI's text-embedding-3-small model)
- 💾 Efficient local storage using Room database
- 🎨 Modern UI with Jetpack Compose
- 🔄 Offline-first architecture with proper error handling

## Architecture

The app follows Clean Architecture principles with MVVM pattern:

- **UI Layer**: Jetpack Compose UI components and ViewModels
- **Domain Layer**: Use cases and business logic interfaces
- **Data Layer**: Repository implementations, local database, and network services

### Key Components

- `VoiceMemoViewModel`: Manages UI state and business logic
- `VoiceMemoRepository`: Coordinates between local storage and network services
- `AudioRecordingService`: Handles voice recording functionality
- `WhisperTranscriptionService`: Manages audio transcription via OpenAI
- `OpenAIEmbeddingService`: Generates text embeddings
- `PineconeStorageService`: Handles vector storage and retrieval

## Setup

1. Clone the repository
2. Copy `local.properties.template` to `local.properties` and fill in your configuration:
   ```properties
   # Android SDK location
   sdk.dir=/path/to/your/Android/sdk
   ndk.dir=/path/to/your/Android/sdk/ndk

   # API Keys and Configuration
   OPENAI_API_KEY=your_openai_api_key
   OPENAI_PROJECT_ID=your_openai_project_id
   PINECONE_API_KEY=your_pinecone_api_key
   PINECONE_INDEX=your_pinecone_index
   PINECONE_ENVIRONMENT=your_pinecone_environment
   PINECONE_HOST_URL=your_pinecone_host_url
   ```
3. Build and run the project

## Requirements

- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- OpenAI API key with access to Whisper and embeddings
- Pinecone API key and index

## Testing

The project includes both unit tests and integration tests:

- Unit tests for repository and service layers
- Integration tests for the full recording-to-storage flow
- Mock implementations for network services in tests

## Error Handling

The app implements comprehensive error handling:
- Network errors with offline support
- Audio recording errors
- API rate limiting and quota management
- Proper error messages to users

## Performance Considerations

- Efficient memory usage with audio file cleanup
- Proper coroutine usage for async operations
- Flow-based reactive architecture
- Optimized database queries

## Security

- API keys stored securely in local.properties
- Network calls over HTTPS
- Proper permission handling
- No sensitive data stored locally

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- OpenAI for the Whisper API
- Pinecone for vector database services
- Android Jetpack Compose team for the modern UI framework 