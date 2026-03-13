package org.kulus.android.data.api.v3

import org.kulus.android.data.api.v3.dto.PostReadingRequest
import org.kulus.android.data.api.v3.dto.PostReadingResponse
import org.kulus.android.data.api.v3.dto.ReadingsResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Kulus v3 API Service
 *
 * Base URL: https://kulus-api.azurewebsites.net/api/v3
 * Authentication: x-api-key header (no token management needed)
 */
interface KulusV3ApiService {

    /**
     * Get glucose readings for a user
     * @param userId Phone number in E.164 format (e.g., +16464849595)
     * @param limit Maximum number of readings to return (default 100)
     * @param offset Pagination offset
     * @param startDate ISO8601 date to filter from
     * @param endDate ISO8601 date to filter to
     */
    @GET("readings")
    suspend fun getReadings(
        @Query("phone") userId: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ReadingsResponse>

    /**
     * Post a new glucose reading
     * @param request The reading data
     */
    @POST("readings")
    suspend fun postReading(
        @Body request: PostReadingRequest
    ): Response<PostReadingResponse>
}
