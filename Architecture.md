**Architecture.md**

# Architecture

## High-Level Diagram

+----------------+ +----------------------+ | Android Device | | Pinecone Storage | | (UI & Logic) | -----> | (Transcriptions Only)| +----------------+ +----------------------+ | | (New Feature: Find Todos) v +----------------+ +----------------------+ | Todo Extraction| | Google Sheets Module | | Module | -----> | (Todo Storage) | +----------------+ +----------------------+


## Components

### Existing Components
1. **UI Layer (Android)**
   - **MainActivity**: Manages recording, transcription, and file management.
   - **VoiceMemoScreen**: For displaying recordings and transcriptions.
   - **TodoScreen**: For displaying and managing todos extracted from transcripts.

2. **Audio Capture Module**
   - Uses Android's `MediaRecorder` to capture audio.
   - Stores temporary audio files in app-specific storage.

3. **Transcription Module**
   - Sends audio data to OpenAI Whisper for transcription.

4. **Data Storage Module**
   - **PineconeClient**: Inserts transcribed text (converted to vector embeddings) into Pinecone.

### New Components (MVP Level)
1. **Todo Extraction Module**
   - **Function**: Sends a selected transcript to OpenAI with a prompt to extract todo items.
   - **Output**: Receives a JSON object formatted as:
     ```json
     { "todos": [ { "item": "Buy groceries", "timeEstimate": "30 minutes" }, ... ] }
     ```
   - **Integration**: This module is separate from the transcription process and focuses solely on actionable tasks.

2. **Google Sheets Integration Module**
   - **Function**: Records the final, user-modified todo items into a designated Google Sheet.
   - **Note**: For the MVP, this is the primary method for storing and later processing todos manually.

## Data Flow

1. **Recording & Transcription Flow** (Existing)
   - User records audio → Chooses to keep recording → Audio sent to Whisper → Transcription displayed → (Optionally) saved in Pinecone.

2. **Find Todos Flow** (New)
   - User selects an existing transcript.
   - The transcript is sent to the Todo Extraction Module.
   - OpenAI processes the transcript and returns a JSON list of todos.
   - The editable todo list is displayed for user review (add/edit/delete).
   - Upon confirmation, the final todo list is sent to the Google Sheets Integration Module.
   - The todos are recorded into the target Google Sheet for further processing.
   - After saving, the UI navigates back to the transcripts listing.

## Extensibility Notes
- The system is designed to be modular so that new AI-powered features (like converting transcripts to social media posts or tasks) can be added later.
- While Pinecone remains the storage for transcriptions, the todos are managed separately through Google Sheets, reflecting the different use cases.
- Given the MVP stage, the focus is on simplicity and ease of use for personal productivity.

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