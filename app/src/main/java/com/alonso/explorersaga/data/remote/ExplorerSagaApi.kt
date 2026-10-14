package com.alonso.explorersaga.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class CategoryResponseDto(
    val id: Long,
    val name: String,
    val description: String? = null
)

@Serializable
data class PlaceResponseDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val category: CategoryResponseDto? = null
)

@Serializable
data class ImportResultDto(
    val processed: Int = 0,
    val inserted: Int = 0,
    val updated: Int = 0,
    val skipped: Int = 0,
    val failed: Int = 0
)

interface ExplorerSagaApi {
    @GET("api/v1/places")
    suspend fun getPlaces(@Query("category") category: String? = null): List<PlaceResponseDto>

    @GET("api/v1/places/{id}")
    suspend fun getPlaceById(@Path("id") id: Long): PlaceResponseDto

    @POST("api/v1/places/import")
    suspend fun importPlaces(): ImportResultDto
}

object RetrofitClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val api: ExplorerSagaApi by lazy {
        retrofit.create(ExplorerSagaApi::class.java)
    }
}
