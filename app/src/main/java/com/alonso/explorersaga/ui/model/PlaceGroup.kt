package com.alonso.explorersaga.ui.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.alonso.explorersaga.R

/** Grupos que ve el usuario. La clave coincide con "group" del paquete de contenido. */
enum class PlaceGroup(
    @StringRes val label: Int,
    val icon: ImageVector,
    val color: Color
) {
    HERITAGE(R.string.group_heritage, Icons.Filled.Castle, Color(0xFFB5651D)),
    MUSEUMS(R.string.group_museums, Icons.Filled.Museum, Color(0xFF8E3B7A)),
    NATURE(R.string.group_nature, Icons.Filled.Forest, Color(0xFF2E7D32)),
    BATHING(R.string.group_bathing, Icons.Filled.Pool, Color(0xFF0277BD)),
    FOOD(R.string.group_food, Icons.Filled.Restaurant, Color(0xFFC62828)),
    SLEEP(R.string.group_sleep, Icons.Filled.Bed, Color(0xFF5D4037)),
    SHOPS(R.string.group_shops, Icons.Filled.ShoppingBag, Color(0xFFEF6C00)),
    SERVICES(R.string.group_services, Icons.Filled.LocalPharmacy, Color(0xFF546E7A));

    val key: String get() = name

    companion object {
        /** "Todo" en Explorar y el mapa: todo menos los servicios (farmacias, cargadores…). */
        val DISCOVER = entries - SERVICES
        val SIGHTS = listOf(HERITAGE, MUSEUMS, NATURE, BATHING)

        fun fromKey(key: String): PlaceGroup? = entries.firstOrNull { it.name == key }
    }
}

data class CategoryInfo(@StringRes val label: Int, val icon: ImageVector)

private val CATEGORIES = mapOf(
    "castle" to CategoryInfo(R.string.cat_castle, Icons.Filled.Castle),
    "monument" to CategoryInfo(R.string.cat_monument, Icons.Filled.AccountBalance),
    "archaeology" to CategoryInfo(R.string.cat_archaeology, Icons.Filled.AccountBalance),
    "religious" to CategoryInfo(R.string.cat_religious, Icons.Filled.Church),
    "palace" to CategoryInfo(R.string.cat_palace, Icons.Filled.AccountBalance),
    "bridge" to CategoryInfo(R.string.cat_bridge, Icons.Filled.AccountBalance),
    "museum" to CategoryInfo(R.string.cat_museum, Icons.Filled.Museum),
    "gallery" to CategoryInfo(R.string.cat_gallery, Icons.Filled.Museum),
    "viewpoint" to CategoryInfo(R.string.cat_viewpoint, Icons.Filled.Visibility),
    "protected_area" to CategoryInfo(R.string.cat_protected_area, Icons.Filled.Park),
    "waterfall" to CategoryInfo(R.string.cat_waterfall, Icons.Filled.Water),
    "cave" to CategoryInfo(R.string.cat_cave, Icons.Filled.Terrain),
    "peak" to CategoryInfo(R.string.cat_peak, Icons.Filled.Landscape),
    "attraction" to CategoryInfo(R.string.cat_attraction, Icons.Filled.Place),
    "bathing" to CategoryInfo(R.string.cat_bathing, Icons.Filled.Pool),
    "restaurant" to CategoryInfo(R.string.cat_restaurant, Icons.Filled.Restaurant),
    "cafe" to CategoryInfo(R.string.cat_cafe, Icons.Filled.Coffee),
    "bar" to CategoryInfo(R.string.cat_bar, Icons.Filled.LocalBar),
    "ice_cream" to CategoryInfo(R.string.cat_ice_cream, Icons.Filled.Icecream),
    "hotel" to CategoryInfo(R.string.cat_hotel, Icons.Filled.Bed),
    "hostel" to CategoryInfo(R.string.cat_hostel, Icons.Filled.Bed),
    "rural" to CategoryInfo(R.string.cat_rural, Icons.Filled.Bed),
    "apartment" to CategoryInfo(R.string.cat_apartment, Icons.Filled.Bed),
    "camping" to CategoryInfo(R.string.cat_camping, Icons.Filled.Forest),
    "gift" to CategoryInfo(R.string.cat_gift, Icons.Filled.ShoppingBag),
    "local_products" to CategoryInfo(R.string.cat_local_products, Icons.Filled.Store),
    "books" to CategoryInfo(R.string.cat_books, Icons.Filled.MenuBook),
    "tourist_info" to CategoryInfo(R.string.cat_tourist_info, Icons.Filled.Info),
    "pharmacy" to CategoryInfo(R.string.cat_pharmacy, Icons.Filled.LocalPharmacy),
    "charging" to CategoryInfo(R.string.cat_charging, Icons.Filled.EvStation),
    "health" to CategoryInfo(R.string.cat_health, Icons.Filled.LocalHospital)
)

fun categoryInfo(category: String): CategoryInfo =
    CATEGORIES[category] ?: CategoryInfo(R.string.cat_attraction, Icons.Filled.Place)
