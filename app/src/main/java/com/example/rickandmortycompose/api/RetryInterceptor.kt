package com.example.rickandmortycompose.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.random.Random

/**
 * OkHttp Interceptor that retries failed requests with exponential backoff and jitter.
 * Retries on 5xx server errors and IOExceptions (network errors).
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null
        var lastResponse: Response? = null

        repeat(maxRetries) { attempt ->
            // Close previous response if any
            lastResponse?.close()

            try {
                val response = chain.proceed(request)

                // Don't retry on success or client errors (4xx)
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }

                // Retry on server errors (5xx)
                if (response.code in 500..599) {
                    lastResponse = response
                    if (attempt < maxRetries - 1) {
                        val delay = calculateDelayWithJitter(attempt)
                        Thread.sleep(delay)
                    }
                } else {
                    return response
                }
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    val delay = calculateDelayWithJitter(attempt)
                    Thread.sleep(delay)
                }
            }
        }

        // If we exhausted all retries
        lastResponse?.let { return it }
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }

    /**
     * Calculates delay with exponential backoff and ±25% jitter.
     * delay = baseDelay * 2^attempt * (0.75 to 1.25)
     */
    private fun calculateDelayWithJitter(attempt: Int): Long {
        val exponentialDelay = baseDelayMs * (1 shl attempt) // 2^attempt
        val jitterFactor = 0.75 + Random.nextDouble() * 0.5 // 0.75 to 1.25
        return (exponentialDelay * jitterFactor).toLong()
    }
}
