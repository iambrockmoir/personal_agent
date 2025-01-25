# Specification

## Overview
This project aims to build a simple Android app for recording voice memos, transcribing them using OpenAI Whisper, and saving the resulting text in Pinecone for indexing. Users should also be able to discard recordings before transcription if they don’t wish to keep them.

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
   - As a user, I want to delete a recording before transcription if I decide it’s not needed.
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