package com.personal.voicememo.data

import kotlinx.coroutines.flow.*
import java.net.UnknownHostException

/**
 * A generic class that provides an offline-first implementation for network-bound resources.
 * 
 * This class implements the recommended architecture for network-bound resources from
 * the Android Architecture Components. It provides an opinionated implementation that:
 * 1. Loads data from database first
 * 2. Then tries to fetch from network
 * 3. Saves network data to database
 * 4. Re-emits from database
 *
 * The implementation handles offline scenarios gracefully by continuing to emit from database
 * when network requests fail.
 *
 * @param ResultType The type of data stored in the database
 * @param RequestType The type of data fetched from the network
 */
abstract class NetworkBoundResource<ResultType, RequestType> {

    private var result: Flow<Result<ResultType>> = flow {
        emit(Result.Loading)

        // First, load from database
        val dbSource = loadFromDb()
        emit(Result.Success(dbSource.first()))

        try {
            // Then try to fetch from network
            val networkResult = fetchFromNetwork()
            
            // Save the network result
            saveNetworkResult(networkResult)
            
            // Re-emit from database after saving network result
            emitAll(loadFromDb().map { Result.Success(it) })
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> {
                    // If offline, continue emitting from database
                    emitAll(loadFromDb().map { Result.Success(it) })
                }
                else -> {
                    emit(Result.Failure(e))
                    // Still emit from database even if network request failed
                    emitAll(loadFromDb().map { Result.Success(it) })
                }
            }
        }
    }

    /**
     * Loads data from the local database.
     * @return Flow of data from database
     */
    protected abstract suspend fun loadFromDb(): Flow<ResultType>

    /**
     * Fetches fresh data from the network.
     * @return Network response
     * @throws Exception if the network request fails
     */
    protected abstract suspend fun fetchFromNetwork(): RequestType

    /**
     * Saves the network response to the database.
     * @param result The network response to save
     */
    protected abstract suspend fun saveNetworkResult(result: RequestType)

    /**
     * Returns the resource as a Flow that can be collected to receive updates.
     * @return Flow of Result<ResultType>
     */
    fun asFlow() = result
} 