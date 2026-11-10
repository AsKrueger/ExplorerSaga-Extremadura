package com.alonso.explorersaga.data.content

import com.alonso.explorersaga.data.db.ContentMeta
import com.alonso.explorersaga.data.db.EventEntity
import com.alonso.explorersaga.data.db.Municipality
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.data.db.PlaceFts
import com.alonso.explorersaga.data.db.RouteEntity
import com.alonso.explorersaga.util.foldForSearch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

data class ContentEntities(
    val places: List<Place>,
    val fts: List<PlaceFts>,
    val routes: List<RouteEntity>,
    val events: List<EventEntity>,
    val municipalities: List<Municipality>,
    val meta: ContentMeta
)

object ContentImporter {

    const val SUPPORTED_SCHEMA = 1

    val json = Json { ignoreUnknownKeys = true }

    // Palabras en los dos idiomas para que "museo", "castle" o "farmacia" encuentren su categoría
    private val CATEGORY_KEYWORDS = mapOf(
        "castle" to "castillo castle fortaleza", "monument" to "monumento monument",
        "archaeology" to "yacimiento arqueologico romano archaeological roman",
        "religious" to "iglesia ermita catedral convento monasterio church chapel",
        "palace" to "palacio palace casa", "bridge" to "puente bridge",
        "museum" to "museo museum", "gallery" to "galeria arte gallery art",
        "viewpoint" to "mirador viewpoint", "protected_area" to "parque reserva natural park nature",
        "waterfall" to "cascada chorrera waterfall", "cave" to "cueva cave", "peak" to "pico sierra peak",
        "attraction" to "atraccion attraction", "bathing" to "piscina natural playa bano charco swimming beach",
        "restaurant" to "restaurante comer restaurant", "cafe" to "cafeteria cafe",
        "bar" to "bar tapas pub", "ice_cream" to "heladeria helado ice cream",
        "hotel" to "hotel hostal", "hostel" to "albergue hostel", "rural" to "casa rural guest house",
        "apartment" to "apartamento apartment", "camping" to "camping campsite",
        "gift" to "souvenir regalo gift", "local_products" to "productos locales queso vino jamon deli",
        "books" to "libreria books", "tourist_info" to "oficina turismo tourist information",
        "pharmacy" to "farmacia pharmacy", "charging" to "cargador electrico charging",
        "health" to "hospital centro salud health clinic"
    )

    @OptIn(ExperimentalSerializationApi::class)
    fun readPack(gzipped: InputStream): ContentPack =
        GZIPInputStream(gzipped).use { json.decodeFromStream<ContentPack>(it) }

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun toEntities(pack: ContentPack, importedAt: Long): ContentEntities {
        require(pack.schema == SUPPORTED_SCHEMA) { "Esquema de contenido no compatible: ${pack.schema}" }
        val places = pack.places.map { it.toEntity() }
        val fts = places.mapIndexed { index, place ->
            val text = listOfNotNull(place.name, place.nameEn, place.municipality, CATEGORY_KEYWORDS[place.category])
                .joinToString(" ")
                .foldForSearch()
            PlaceFts(rowId = index + 1, placeId = place.id, text = text)
        }
        val routes = pack.routes.mapIndexed { index, route ->
            RouteEntity(
                id = route.id, position = index, mode = route.mode, km = route.km,
                titleEs = route.titleEs, titleEn = route.titleEn,
                durationEs = route.durationEs, durationEn = route.durationEn,
                summaryEs = route.summaryEs, summaryEn = route.summaryEn,
                stopsJson = json.encodeToString(route.stops)
            )
        }
        val events = pack.events.map {
            EventEntity(
                id = it.id, kind = it.kind, fromDay = it.from, toDay = it.to, place = it.place,
                latitude = it.lat, longitude = it.lon, titleEs = it.titleEs, titleEn = it.titleEn,
                textEs = it.textEs, textEn = it.textEn
            )
        }
        val municipalities = pack.municipalities
            .distinctBy { it.name }
            .map { Municipality(it.name, it.prov, it.lat, it.lon) }
        return ContentEntities(places, fts, routes, events, municipalities, ContentMeta(0, pack.version, importedAt))
    }

    fun decodeStops(stopsJson: String): List<RouteStopDto> = json.decodeFromString(stopsJson)

    private fun PlaceDto.toEntity() = Place(
        id = id, name = name, nameEn = nameEn, category = cat, groupKey = group,
        latitude = lat, longitude = lon, municipality = muni, province = prov,
        descriptionEs = descEs, descriptionEn = descEn, address = addr, website = web,
        phone = phone, openingHours = hours, wheelchair = wheelchair, wikidata = qid,
        wikipediaEs = wpEs, wikipediaEn = wpEn, heritage = heritage, official = official,
        imageUrl = img, imagePage = imgPage, imageAuthor = imgAuthor, imageLicense = imgLicense,
        score = score, sources = src.joinToString(",")
    )
}
