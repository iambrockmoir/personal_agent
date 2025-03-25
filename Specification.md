# Specification

## Overview
This project aims to build a simple Android app for recording voice memos, transcribing them using OpenAI Whisper, and saving the resulting text in Pinecone for indexing. Users should also be able to discard recordings before transcription if they don't wish to keep them.

## Goals and Objectives
1. **Voice Memo Recording**: Allow users to record audio memos.
2. **Discard Option**: Provide a way to delete the recording before the transcription step.
3. **Speech-to-Text**: Transcribe audio using OpenAI Whisper.
4. **Data Storage**: Store transcribed text in Pinecone (and optionally local or remote DB).
5. **MVP UI**: Keep the interface minimal with essential controls.
6. **Extensibility**: Ensure the architecture is flexible for future features like note linking, social media copy, and AI-driven processing.

## Requirements
- **Android Device**: Pixel 3 or higher.
- **Speech-to-Text**: OpenAI Whisper (remote or local inference via whisper.cpp if feasible).
- **Data Indexing**: Pinecone for storing transcribed text vectors.
- **UI**: Simple screen with recording, deletion, and save actions.
- **Budget**: Minimal—emphasis on MVP capabilities.
- **Deployment**: Primarily for personal use, sideloaded APK or internal test track on Google Play.

## User Stories
1. **Record Memo**  
   - As a user, I want to record my voice so I can capture thoughts on the go.
2. **Discard Recording**  
   - As a user, I want to delete a recording before transcription if I decide it's not needed.
3. **Transcribe and Save**  
   - As a user, I want my recording transcribed and the text saved so I can reference or search it later.
4. **Extensible AI** (Future)  
   - As a user, I want to extend these transcripts into tasks, social media posts, or other structured formats with minimal overhead.

## Constraints
- **Performance**: Minimal overhead on a Pixel 3 phone.
- **Costs**: Minimize expenses for Whisper API calls (consider local models if necessary).
- **Privacy**: Limited regulatory concerns for an MVP, but handle user recordings responsibly.

## Success Criteria
- The app successfully records audio and prompts to keep or discard.
- If kept, the audio is transcribed accurately (within reason for an MVP).
- The transcribed text is stored in Pinecone for later retrieval or search.
- Basic testing is in place to validate core functionality.

## Potential TODOS

- [x] Enhance error handling in the transcription module (callWhisperAPI) to manage network timeouts and API failures. (Current implementation uses basic error messaging via handleError.)
- [x] Add UI feedback (e.g., a loading spinner) during lengthy operations like transcription.
- [x] Add status updates to the UI to indicate that the app is processing the recording.
- [x] Add visual feedback for file processing (e.g., display current file name and a progress indicator) during save/transcription.
- [x] Implement discard functionality for audio recordings. (Handled via discardRecording in the ViewModel and file deletion in the UI.)
- [ ] Implement proper text-to-vector conversion (convertTextToVector) in the repository's saveToVectorDB flow. (Currently stubbed behavior.)
- [x] Ensure proper management and cleanup of temporary audio files after either the 'Keep' or 'Delete' actions. (File deletions are implemented in the UI and in the Application onTerminate.)
- [ ] Expand unit and integration tests to cover error scenarios and validate the complete voice memo flow (record, prompt, transcribe, and save). (Some unit tests exist; further integration tests can be added.)
- [ ] Add functionality for a brief summary of the transcription to be displayed in the UI.
- [ ] Add functionality for a "chat" interface to allow the user to ask questions of their memos.
- [ ] Add functionality to organize groups of memos into collections
- [ ] Implement offline support using NetworkBoundResource for better offline-first experience
- [ ] Add UI tests with Espresso for critical user flows
- [ ] Enhance error handling with more specific error types and recovery strategies
- [ ] Add retry mechanisms for failed network operations
- [ ] Implement proper error state management in the UI (e.g., retry buttons for failed operations)
- [ ] Add analytics to track usage patterns and error rates
- [ ] Implement proper cleanup of old recordings and transcripts to manage storage space
- [ ] Add export functionality for memos (e.g., as text files or PDFs)
- [ ] Implement proper audio format validation and conversion if needed
- [ ] Add support for different audio quality settings
- [ ] Implement proper handling of device storage space warnings