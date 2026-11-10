package com.alonso.explorersaga.data.content

import kotlinx.serialization.Serializable

/** Paquete de contenido generado por tools/datafactory (schema 1). */
@Serializable
data class ContentPack(
    val schema: Int,
    val version: Long,
    val attribution: List<String> = emptyList(),
    val municipalities: List<MunicipalityDto> = emptyList(),
    val places: List<PlaceDto> = emptyList(),
    val routes: List<RouteDto> = emptyList(),
    val events: List<EventDto> = emptyList()
)

@Serializable
data class ContentManifest(
    val schema: Int,
    val version: Long,
    val file: String,
    val sha256: String,
    val size: Long,
    val places: Int = 0
)

@Serializable
data class MunicipalityDto(
    val name: String,
    val prov: String? = null,
    val lat: Double,
    val lon: Double
)

@Serializable
data class PlaceDto(
    val id: String,
    val name: String,
    val nameEn: String? = null,
    val cat: String,
    val group: String,
    val lat: Double,
    val lon: Double,
    val muni: String,
    val prov: String? = null,
    val descEs: String? = null,
    val descEn: String? = null,
    val addr: String? = null,
    val web: String? = null,
    val phone: String? = null,
    val hours: String? = null,
    val wheelchair: String? = null,
    val qid: String? = null,
    val wpEs: String? = null,
    val wpEn: String? = null,
    val heritage: Boolean = false,
    val official: String? = null,
    val img: String? = null,
    val imgPage: String? = null,
    val imgAuthor: String? = null,
    val imgLicense: String? = null,
    val score: Int = 0,
    val src: List<String> = emptyList()
)

@Serializable
data class RouteDto(
    val id: String,
    val mode: String,
    val km: Double,
    val titleEs: String,
    val titleEn: String,
    val durationEs: String,
    val durationEn: String,
    val summaryEs: String,
    val summaryEn: String,
    val stops: List<RouteStopDto>
)

@Serializable
data class RouteStopDto(
    val name: String,
    val nameEn: String? = null,
    val lat: Double,
    val lon: Double,
    val muni: String? = null,
    val placeId: String? = null
)

@Serializable
data class EventDto(
    val id: String,
    val kind: String,
    val from: String,
    val to: String,
    val place: String,
    val lat: Double,
    val lon: Double,
    val titleEs: String,
    val titleEn: String,
    val textEs: String,
    val textEn: String
)
