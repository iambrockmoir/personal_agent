# Acceptance Criteria

Below are criteria that define when this MVP is considered complete and ready for usage and potential future enhancements.

## 1. Recording & Discarding
- **Criterion**: When the user taps `Record`, the app successfully captures audio.
- **Criterion**: Upon stopping the recording, a prompt must appear asking the user to keep or delete the recording.
- **Criterion**: If the user selects `Delete`, the recorded audio is discarded, and no transcription request is made.

## 2. Transcription
- **Criterion**: If the user selects `Keep`, the audio file is passed to Whisper (either remote or local).
- **Criterion**: The system successfully receives a text transcription and displays it on the screen.
- **Criterion**: The transcription should be legible and relevant to the user’s audio input (exact accuracy depends on Whisper’s capabilities).

## 3. Saving & Indexing
- **Criterion**: When the user taps `Save`, the transcribed text is sent to Pinecone.
- **Criterion**: The text is embedded into a vector (if applicable) before insertion.
- **Criterion**: A confirmation message is displayed indicating successful storage.

## 4. UI/UX
- **Criterion**: The main screen shows a clear record button, a stop button, and displays text after transcription.
- **Criterion**: The user receives visual or auditory feedback for each major step (record, discard, transcribe, save).

## 5. Testing
- **Criterion**: At least one unit test verifying transcription logic (mocking Whisper).
- **Criterion**: At least one integration test covering the full flow (record, keep, transcribe, save).
- **Criterion**: The app can be sideloaded onto a Pixel 3 device without runtime errors.

## 6. Future Extensibility
- **Criterion**: Code modules (Audio, Transcription, Storage) are loosely coupled, allowing addition of new features (e.g., social media post generation, note linking).
- **Criterion**: The user interface is simple but can be expanded easily with additional actions or screens.

---

## Sign-Off
Once all these criteria are met, the MVP will be deemed complete for the initial release. Subsequent iterations may add advanced features, improved AI capabilities, and additional storage or search functionalities.