package com.alonso.explorersaga.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StorageTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun sizeOf_addsUpNestedFiles() {
        val root = temp.newFolder("cache")
        File(root, "a.bin").writeBytes(ByteArray(100))
        File(root, "images/b.bin").apply { parentFile!!.mkdirs() }.writeBytes(ByteArray(250))

        assertEquals(350L, sizeOf(root))
    }

    @Test
    fun sizeOf_isZeroForMissingFiles() {
        assertEquals(0L, sizeOf(File(temp.root, "missing")))
    }

    @Test
    fun deleteContents_emptiesTheFolderButKeepsIt() {
        val root = temp.newFolder("cache")
        File(root, "a.bin").writeBytes(ByteArray(10))
        File(root, "tiles/b.bin").apply { parentFile!!.mkdirs() }.writeBytes(ByteArray(10))

        deleteContents(root)

        assertTrue(root.exists())
        assertEquals(0, root.listFiles()!!.size)
    }
}
