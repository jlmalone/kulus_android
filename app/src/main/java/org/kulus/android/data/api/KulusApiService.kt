package org.kulus.android.data.api

import org.kulus.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface KulusApiService {

    @POST("/validatePassword")
    suspend fun authenticate(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/verifyToken")
    suspend fun verifyToken(@Body request: VerifyTokenRequest): Response<VerifyTokenResponse>

    @GET("/api/v2/getAllReadings")
    suspend fun getAllReadings(): Response<KulusReadingsResponse>

    @GET("/api/v2/addReadingFromUrl")
    suspend fun addReading(
        @Query("name") name: String,
        @Query("reading") reading: String,
        @Query("units") units: String = "mmol/L",
        @Query("comment") comment: String? = null,
        @Query("snackPass") snackPass: Boolean = false,
        @Query("source") source: String = "android"
    ): Response<AddReadingResponse>
}
