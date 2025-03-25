package com.personal.voicememo.data

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [T]
 * or a failure with an arbitrary [Throwable] exception.
 *
 * This class is used throughout the application to handle operation results and propagate errors.
 * It provides a type-safe way to handle both successful and failed operations.
 *
 * @param T The type of successful operation result
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data of type [T].
     *
     * @property data The operation result
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation.
     *
     * @property exception The error that caused the operation to fail
     */
    data class Failure(val exception: Throwable) : Result<Nothing>()

    /**
     * Represents an operation in progress.
     * Used primarily with Flow to indicate that an operation has started but hasn't completed.
     */
    object Loading : Result<Nothing>()

    /** @return true if this instance represents a successful outcome */
    val isSuccess: Boolean
        get() = this is Success

    /** @return true if this instance represents a failed outcome */
    fun isError() = this is Failure

    /** @return true if this instance represents a loading state */
    fun isLoading() = this is Loading

    /**
     * Returns the encapsulated value if this instance represents [Success] or null otherwise.
     * Useful for when null is a valid value for failure cases.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [Failure] or null otherwise.
     * Useful for error handling and logging.
     */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Failure -> exception
        else -> null
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> failure(exception: Throwable): Result<T> = Failure(exception)
    }
} 