# Architecture

## High-Level Diagram

+----------------+ +----------------------+
| Android Device | | Pinecone (Storage) | | (UI & Logic) | -----> | Vector DB Index | +----------------+ +----------------------+ | v Audio Data | +-> [Transcription Module - Whisper API/Library] returns text | +-> [UI Display & Optional Edit] | +-> [Storage: Pinecone Insert]

markdown
Copy
Edit

## Components

1. **UI Layer (Android)**
   - **MainActivity** / **RecordActivity**: Handles button interactions, audio recording, and user prompts.
   - **Fragments or Views**: Could include separate fragments for recording and display, depending on app structure.

2. **Audio Capture Module**
   - Uses Android's `MediaRecorder` or `AudioRecord` to record audio.
   - Stores temporary audio file(s) in app-specific storage.

3. **Transcription Module**
   - **Option A (Remote)**: A small API client wrapper that sends audio data to OpenAI Whisper or an external server hosting Whisper.
   - **Option B (Local)**: Integrated library for on-device inference (e.g., [whisper.cpp](https://github.com/ggerganov/whisper.cpp)).

4. **Data Storage Module**
   - **PineconeClient**: A networking layer to connect to Pinecone.
   - **Embedding Service** (Optional in MVP): Convert text to vector embeddings before Pinecone insertion.

5. **Testing & Deployment**
   - **Testing**: JUnit for unit tests, possibly Espresso for UI tests.
   - **Deployment**: 
     - **Local**: Sideload the APK onto your Pixel 3.
     - **Alternative**: Internal Google Play test track for quick updates.

## Data Flow
1. **Record**: User taps record → audio data is captured.
2. **Stop & Prompt**: User chooses to keep or discard.
3. **Transcribe**: If kept, audio is sent to Whisper for transcription.
4. **Display & Edit**: The text is shown on-screen.
5. **Store**: When saved, text is (optionally embedded) and inserted into Pinecone.
6. **Index & Future Use**: The text is now searchable and can be extended with future AI functionalities.

## Extensibility Notes
- Code structure should remain modular to allow adding new "actions" on the transcribed text (e.g., convert to social media post, link images, create tasks).
- Networking calls can be expanded to incorporate other AI services like Anthropic.
- Additional data layers (like Supabase or local SQLite) can be integrated if offline support or more robust data retrieval is required.

## Planned Improvements

### Offline Support
- Implement NetworkBoundResource pattern for offline-first experience
- Add local caching for audio files and transcriptions
- Implement background sync for pending operations
- Add conflict resolution for offline changes

### Testing & Quality
- Add comprehensive UI tests with Espresso
- Implement end-to-end testing for critical flows
- Add performance monitoring and analytics
- Enhance error tracking and reporting

### Storage & Performance
- Implement proper cleanup of old recordings
- Add storage space management
- Optimize audio file handling
- Add support for different audio quality settings

### User Experience
- Add retry mechanisms for failed operations
- Implement proper error state management
- Add export functionality
- Support for organizing memos into collections
- Add chat interface for memo interaction

### Security & Privacy
- Enhance audio file validation
- Implement proper storage space warnings
- Add data export/backup functionality
- Consider encryption for sensitive data

### Analytics & Monitoring
- Track usage patterns
- Monitor error rates
- Track performance metrics
- Implement crash reporting