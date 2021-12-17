/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.MediaMerge
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class MergeMediaTest {

    val directoryProvider = Mockito.mock(IDirectoryProvider::class.java)

    val fromFile = createTempFile("fromRc", ".zip")
    val toFile = createTempFile("toRc", ".zip")

    init {
        javaClass.getResource("resource-containers/en_ulb_media_merge_test.zip").openStream().use { ifs ->
            fromFile.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }
        javaClass.getResource("resource-containers/en_ulb.zip").openStream().use { ifs ->
            toFile.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }
        fromFile.deleteOnExit()
        toFile.deleteOnExit()
    }

    @Test
    fun testMerge() {
        val fromRc = ResourceContainer.load(fromFile)
        val toRc = ResourceContainer.load(toFile)

        val mergeMedia = MediaMerge(directoryProvider, fromRc, toRc)
        mergeMedia.merge()
        fromRc.write()
        assertTrue(validateAfterWrite(toRc))
        toFile.deleteRecursively()
    }

    fun validateAfterWrite(rc: ResourceContainer): Boolean {
        val reparse = ResourceContainer.load(rc.file)
        assertTrue((reparse.media == null) == (rc.media == null))
        assertTrue(reparse.accessor.fileExists("media/num/chapter.mp3"))
        assertTrue(reparse.accessor.fileExists("media/lev/chapter.mp3"))
        return true
    }
}