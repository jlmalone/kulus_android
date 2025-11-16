package org.kulus.android.data.api

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.kulus.android.BuildConfig
import org.kulus.android.data.local.TokenStore
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("x-api-key", BuildConfig.API_KEY)
            .header("Accept", "application/json")

        // Add auth token if available (blocking call for simplicity)
        val token = runBlocking {
            tokenStore.getToken().first()
        }

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // Add content-type for requests with body
        if (originalRequest.body != null) {
            requestBuilder.header("Content-Type", "application/json")
        }

        return chain.proceed(requestBuilder.build())
    }
}
