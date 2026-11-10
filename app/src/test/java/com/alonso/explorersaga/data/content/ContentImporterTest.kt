package com.alonso.explorersaga.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class ContentImporterTest {

    private val packJson = """
        {"schema":1,"version":20261110,"attribution":["OSM"],
         "municipalities":[{"name":"Cáceres","prov":"CC","lat":39.47,"lon":-6.37},
                           {"name":"Cáceres","prov":"CC","lat":39.47,"lon":-6.37}],
         "places":[{"id":"w1","name":"Torre de Bujaco","cat":"castle","group":"HERITAGE",
                    "lat":39.4748,"lon":-6.3714,"muni":"Cáceres","prov":"CC","heritage":true,
                    "descEs":"Torre almohade.","score":9,"src":["osm","wikidata"],"unknownField":1}],
         "routes":[{"id":"r","mode":"walk","km":3,"titleEs":"T","titleEn":"T","durationEs":"d",
                    "durationEn":"d","summaryEs":"s","summaryEn":"s",
                    "stops":[{"name":"Torre","lat":39.4748,"lon":-6.3714,"placeId":"w1"}]}],
         "events":[{"id":"e","kind":"nature","from":"03-15","to":"04-20","place":"Jerte",
                    "lat":40.1,"lon":-5.8,"titleEs":"Cerezo","titleEn":"Cherry","textEs":"a","textEn":"b"}]}
    """.trimIndent()

    private fun gzip(text: String): ByteArray = ByteArrayOutputStream().also { out ->
        GZIPOutputStream(out).use { it.write(text.toByteArray()) }
    }.toByteArray()

    @Test
    fun readsGzippedPackIgnoringUnknownFields() {
        val pack = ContentImporter.readPack(ByteArrayInputStream(gzip(packJson)))
        assertEquals(20261110L, pack.version)
        assertEquals("Torre de Bujaco", pack.places.single().name)
    }

    @Test
    fun mapsPackToEntitiesWithSearchIndex() {
        val pack = ContentImporter.readPack(ByteArrayInputStream(gzip(packJson)))
        val entities = ContentImporter.toEntities(pack, importedAt = 42)

        val place = entities.places.single()
        assertEquals("HERITAGE", place.groupKey)
        assertTrue(place.heritage)
        assertEquals("osm,wikidata", place.sources)
        assertNull(place.nameEn)

        // El índice no lleva tildes e incluye palabras de la categoría en los dos idiomas
        val fts = entities.fts.single()
        assertEquals("w1", fts.placeId)
        assertTrue(fts.text.contains("caceres"))
        assertTrue(fts.text.contains("castillo castle"))

        assertEquals(1, entities.municipalities.size)
        assertEquals(20261110L, entities.meta.version)
        assertEquals(42L, entities.meta.importedAt)
        assertEquals("w1", ContentImporter.decodeStops(entities.routes.single().stopsJson).single().placeId)
        assertEquals("03-15", entities.events.single().fromDay)
    }

    @Test
    fun rejectsUnsupportedSchema() {
        val pack = ContentPack(schema = 99, version = 1)
        assertThrows(IllegalArgumentException::class.java) { ContentImporter.toEntities(pack, 0) }
    }

    @Test
    fun sha256MatchesKnownValue() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            ContentImporter.sha256("abc".toByteArray())
        )
    }
}
