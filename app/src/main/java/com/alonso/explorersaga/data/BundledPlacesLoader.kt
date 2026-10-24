package com.alonso.explorersaga.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.IOException

// Lee los lugares incluidos en la app (assets), usados para poblar la caché en el primer arranque sin conexión
class BundledPlacesLoader(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<PlaceEntity> = ASSET_FILES.flatMap { fileName ->
        try {
            val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
            json.decodeFromString<List<PlaceEntity>>(text)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    private companion object {
        val ASSET_FILES = listOf("monuments.json", "gastronomia.json", "tiendas.json")
    }
}
