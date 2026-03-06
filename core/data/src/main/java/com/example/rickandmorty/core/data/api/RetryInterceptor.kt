package com.example.rickandmorty.core.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.random.Random

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null
        var lastResponse: Response? = null

        repeat(maxRetries) { attempt ->
            lastResponse?.close()

            try {
                val response = chain.proceed(request)

                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }

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

        lastResponse?.let { return it }
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }

    private fun calculateDelayWithJitter(attempt: Int): Long {
        val exponentialDelay = baseDelayMs * (1 shl attempt)
        val jitterFactor = 0.75 + Random.nextDouble() * 0.5
        return (exponentialDelay * jitterFactor).toLong()
    }
}
