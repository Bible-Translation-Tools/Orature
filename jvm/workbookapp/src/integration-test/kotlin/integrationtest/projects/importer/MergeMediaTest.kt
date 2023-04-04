package integrationtest.projects.importer

/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.MediaMerge
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import javax.inject.Inject

class MergeMediaTest {

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    val fromFile = directoryProvider.createTempFile("fromRc", ".zip")
    val toFile = directoryProvider.createTempFile("toRc", ".zip")

    @Before
    fun setup() {
        javaClass.classLoader.getResource("resource-containers/en_ulb_media_merge_test.zip")
            .openStream().use { ifs ->
                fromFile.outputStream().use { ofs ->
                    ifs.transferTo(ofs)
                }
            }
        javaClass.classLoader.getResource("resource-containers/en_ulb.zip")
            .openStream().use { ifs ->
                toFile.outputStream().use { ofs ->
                    ifs.transferTo(ofs)
                }
            }
    }

    @After
    fun cleanUp() {
        fromFile.deleteRecursively()
        toFile.deleteRecursively()
    }

    @Test
    fun testMerge() {
        val fromRc = ResourceContainer.load(fromFile)
        val toRc = ResourceContainer.load(toFile)

        MediaMerge.merge(fromRc, toRc)
        fromRc.write()
        assertTrue(validateAfterWrite(toRc))
    }

    fun validateAfterWrite(rc: ResourceContainer): Boolean {
        val reparse = ResourceContainer.load(rc.file)
        assertTrue((reparse.media == null) == (rc.media == null))
        assertTrue(reparse.accessor.fileExists("media/num/chapter.mp3"))
        assertTrue(reparse.accessor.fileExists("media/lev/chapter.mp3"))
        return true
    }
}
