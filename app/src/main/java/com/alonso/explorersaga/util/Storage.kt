package com.alonso.explorersaga.util

import java.io.File

// Tamaño total en bytes de un fichero o de todo el contenido de una carpeta (0 si no existe)
fun sizeOf(file: File): Long = when {
    !file.exists() -> 0L
    file.isFile -> file.length()
    else -> file.walkBottomUp().filter { it.isFile }.sumOf { it.length() }
}

// Vacía una carpeta sin borrarla, para que las librerías que la usan sigan encontrándola
fun deleteContents(directory: File) {
    directory.listFiles()?.forEach { it.deleteRecursively() }
}
