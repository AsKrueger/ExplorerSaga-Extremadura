package com.alonso.explorersaga.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

/** Lugar del paquete de contenido. El id es estable entre versiones (tipo + id de OSM). */
@Entity(tableName = "places", indices = [Index("groupKey"), Index("municipality")])
data class Place(
    @PrimaryKey val id: String,
    val name: String,
    val nameEn: String? = null,
    val category: String,
    val groupKey: String,
    val latitude: Double,
    val longitude: Double,
    val municipality: String,
    val province: String? = null,
    val descriptionEs: String? = null,
    val descriptionEn: String? = null,
    val address: String? = null,
    val website: String? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val wheelchair: String? = null,
    val wikidata: String? = null,
    val wikipediaEs: String? = null,
    val wikipediaEn: String? = null,
    val heritage: Boolean = false,
    val official: String? = null,
    val imageUrl: String? = null,
    val imagePage: String? = null,
    val imageAuthor: String? = null,
    val imageLicense: String? = null,
    val score: Int = 0,
    val sources: String = ""
) {
    fun displayName(english: Boolean): String = if (english) nameEn ?: name else name

    /** Descripción en el idioma pedido y, si no la hay, en el otro. */
    fun description(english: Boolean): Pair<String?, Boolean> {
        val preferred = if (english) descriptionEn else descriptionEs
        val other = if (english) descriptionEs else descriptionEn
        return if (preferred != null) preferred to false else other to (other != null)
    }

    fun wikipediaUrl(english: Boolean): String? {
        val (lang, title) = when {
            english && wikipediaEn != null -> "en" to wikipediaEn
            wikipediaEs != null -> "es" to wikipediaEs
            wikipediaEn != null -> "en" to wikipediaEn
            else -> return null
        }
        return "https://$lang.wikipedia.org/wiki/" + java.net.URLEncoder.encode(title.replace(' ', '_'), "UTF-8")
            .replace("%2F", "/").replace("%3A", ":")
    }
}

/** Índice de búsqueda: nombre, nombre en inglés y municipio sin tildes. */
@Fts4(notIndexed = ["placeId"])
@Entity(tableName = "places_fts")
data class PlaceFts(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Int,
    val placeId: String,
    val text: String
)

/** Favorito con su día dentro del plan de viaje (0 = sin día). No lo toca ninguna actualización. */
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val placeId: String,
    val addedAt: Long,
    val day: Int = 0
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val position: Int,
    val mode: String,
    val km: Double,
    val titleEs: String,
    val titleEn: String,
    val durationEs: String,
    val durationEn: String,
    val summaryEs: String,
    val summaryEn: String,
    val stopsJson: String
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val fromDay: String,
    val toDay: String,
    val place: String,
    val latitude: Double,
    val longitude: Double,
    val titleEs: String,
    val titleEn: String,
    val textEs: String,
    val textEn: String
)

@Entity(tableName = "municipalities")
data class Municipality(
    @PrimaryKey val name: String,
    val province: String?,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "content_meta")
data class ContentMeta(
    @PrimaryKey val id: Int = 0,
    val version: Long,
    val importedAt: Long
)
