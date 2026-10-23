package com.alonso.explorersaga.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

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
    val failed: Int = 0,
    val merged: Int = 0
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
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    @OptIn(ExperimentalSerializationApi::class)
    private fun createApi(client: OkHttpClient): ExplorerSagaApi =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ExplorerSagaApi::class.java)

    val api: ExplorerSagaApi by lazy {
        createApi(OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS).build())
    }

    // La importación espera a Overpass API, que con reintentos puede tardar un par de minutos
    val importApi: ExplorerSagaApi by lazy {
        createApi(OkHttpClient.Builder().readTimeout(150, TimeUnit.SECONDS).build())
    }
}
