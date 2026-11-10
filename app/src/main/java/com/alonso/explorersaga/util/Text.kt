package com.alonso.explorersaga.util

import java.text.Normalizer

private val COMBINING_MARKS = "\\p{Mn}+".toRegex()
private val NON_ALPHANUMERIC = "[^a-z0-9]+".toRegex()

/** Minúsculas, sin tildes ni signos: "Cáceres" y "caceres" se buscan igual. */
fun String.foldForSearch(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .replace(NON_ALPHANUMERIC, " ")
        .trim()

/**
 * Convierte lo que escribe el usuario en una consulta FTS4 segura: cada
 * palabra como prefijo ("cace" encuentra "Cáceres") y sin operadores.
 */
fun ftsQuery(input: String): String? {
    val terms = input.foldForSearch().split(' ').filter { it.isNotBlank() }
    if (terms.isEmpty()) return null
    return terms.joinToString(" ") { "$it*" }
}
