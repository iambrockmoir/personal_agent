# Pseudocode

# Pseudocode

## Overview
This pseudocode outlines the core functionality for the MVP, including voice memo recording, transcription, and saving transcriptions. Below, the new **Find Todos** feature is added as an extension.

---

### Existing Core Flow

```plaintext
function onRecordButtonClicked():
    startAudioRecording()               // Begins capturing audio from device microphone

function onStopRecordingButtonClicked():
    stopAudioRecording()                // Ends audio capture
    userChoice = showDialog("Keep or Delete?")

    if userChoice == "KEEP":
        audioData = getRecordedAudio()  // Retrieve the raw audio data
        transcription = callWhisperAPI(audioData)
        displayTranscription(transcription)
    else if userChoice == "DELETE":
        discardRecordedAudio()          // Clean up temporary audio files
        showMessage("Recording discarded")

function onSaveButtonClicked():
    text = getTranscriptionFromUI()     // Retrieve text from the app UI
    storeInPinecone(text)
    showConfirmation("Transcript saved successfully")

function callWhisperAPI(audioData):
    response = whisperApi.transcribe(audioData)
    return response.transcription       // Return the raw text

function storeInPinecone(text):
    vector = convertTextToVector(text)
    pineconeClient.insert(
        vector,
        metadata={ "transcript": text }
    )

---

###  New Feature: Find Todos

function onFindTodosButtonClicked(transcript):
    // Send the transcript to OpenAI with a prompt to extract todos.
    todosJSON = callFindTodosAPI(transcript)
    todos = parseJSON(todosJSON)
    displayEditableTodos(todos)        // Display todos in an editable list for user modifications

function callFindTodosAPI(transcript):
    prompt = "Extract a list of todo items with best time estimates from the following transcript. " +
             "Respond in JSON format as { \"todos\": [ { \"item\": \"...\", \"timeEstimate\": \"...\" } ] }.\n" +
             transcript
    response = openAIClient.sendPrompt(prompt)
    return response.jsonData

function onSaveTodosButtonClicked():
    todos = getUserModifiedTodos()       // Get the edited todo list from the UI
    sendTodosToGoogleSheets(todos)       // Record todos in the target Google Sheet
    showConfirmation("Todos recorded to Google Sheets")
    navigateBackToTranscripts()          // Return to the transcripts listing

function navigateBackToTranscripts():
    // Navigate back to the transcripts screen
    navigationController.navigate(Screen.VoiceMemo.route)

**** Original Pseudocode.md for reference ****

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
```

## Planned Improvements

### Offline Support
```plaintext
function saveMemoWithOfflineSupport(audioData):
    // Save to local storage first
    localId = saveToLocalStorage(audioData)
    
    // Queue for network sync
    queueForSync(localId)
    
    // Start background sync if network available
    if isNetworkAvailable():
        syncPendingChanges()

function syncPendingChanges():
    while hasPendingChanges():
        change = getNextPendingChange()
        try:
            syncToNetwork(change)
            markAsSynced(change)
        catch NetworkError:
            // Keep in queue for retry
            markForRetry(change)
```

### Error Handling & Retry
```plaintext
function handleNetworkOperation(operation):
    maxRetries = 3
    currentRetry = 0
    
    while currentRetry < maxRetries:
        try:
            return operation()
        catch NetworkError:
            currentRetry++
            if currentRetry == maxRetries:
                showError("Operation failed after multiple attempts")
                return null
            waitForRetry(currentRetry)
```

### Storage Management
```plaintext
function manageStorage():
    // Check available space
    if getAvailableSpace() < threshold:
        showStorageWarning()
        
    // Clean up old recordings
    oldRecordings = getRecordingsOlderThan(days=30)
    for recording in oldRecordings:
        if not isPinned(recording):
            deleteRecording(recording)
```

### Analytics & Monitoring
```plaintext
function trackOperation(operation, metadata):
    startTime = getCurrentTime()
    try:
        result = operation()
        logSuccess(operation, startTime, metadata)
        return result
    catch error:
        logError(operation, error, startTime, metadata)
        throw error
```

### Chat Interface
```plaintext
function handleChatQuery(query):
    // Convert query to vector
    queryVector = convertTextToVector(query)
    
    // Search relevant memos
    relevantMemos = searchMemos(queryVector)
    
    // Generate response using relevant context
    response = generateResponse(query, relevantMemos)
    
    return response
```