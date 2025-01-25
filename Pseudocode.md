# Pseudocode

## Overview
Below is a high-level pseudocode outline demonstrating how the Android app handles recording, transcription, discarding, and saving transcripts.

```plaintext
function onRecordButtonClicked():
    startAudioRecording()               // Begins capturing audio from device microphone

function onStopRecordingButtonClicked():
    stopAudioRecording()                // Ends audio capture
    userChoice = showDialog("Keep or Delete?")

    if userChoice == "KEEP":
        audioData = getRecordedAudio()  // Retrieve the raw audio data
        transcription = callWhisperAPI(audioData)
            // Or use local whisper.cpp if desired
        displayTranscription(transcription)
    else if userChoice == "DELETE":
        discardRecordedAudio()          // Clean up temporary audio files
        showMessage("Recording discarded")

function onSaveButtonClicked():
    text = getTranscriptionFromUI()     // Retrieve text from the app UI
    storeInPinecone(text)
    showConfirmation("Transcript saved successfully")

function callWhisperAPI(audioData):
    // Send audio data to the Whisper endpoint
    response = whisperApi.transcribe(audioData)
    return response.transcription       // Return the raw text

function storeInPinecone(text):
    vector = convertTextToVector(text)  // Example: use an embedding model
    pineconeClient.insert(
        vector,
        metadata={ "transcript": text }
    )