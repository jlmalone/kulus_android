package org.kulus.android.data.api.v3

import okhttp3.Interceptor
import okhttp3.Response
import org.kulus.android.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds x-api-key header to all v3 API requests.
 * Much simpler than v2's token-based auth - just a static API key.
 */
@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val requestWithApiKey = originalRequest.newBuilder()
            .header("x-api-key", BuildConfig.API_V3_KEY)
            .header("Content-Type", "application/json")
            .build()

        return chain.proceed(requestWithApiKey)
    }
}
