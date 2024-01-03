/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
package integrationtest.projects.importer

import org.junit.BeforeClass
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.MergeTextContent
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File

const val GENESIS_FILE_PATH = "./01-GEN.txt"
const val EXODUS_FILE_PATH = "./02-EXO.txt"
const val LEVITICUS_FILE_PATH = "./03-LEV.txt"

class TestMergeTextContent {
    companion object {
        val sampleGenesis =
            File.createTempFile("01-GEN", ".txt")
                .apply {
                    writer().use {
                        it.write("In the beginning God created the heavens and the earth.")
                    }
                    deleteOnExit()
                }

        val sampleExodus =
            File.createTempFile("02-EXO", ".txt")
                .apply {
                    writer().use {
                        it.write("Sample text for Exodus")
                    }
                    deleteOnExit()
                }

        val sampleLeviticus =
            File.createTempFile("03-LEV", ".txt")
                .apply {
                    writer().use {
                        it.write("Sample text for Leviticus")
                    }
                    deleteOnExit()
                }

        val overwriteGenesis =
            File.createTempFile("01-GEN", ".txt")
                .apply {
                    writer().use {
                        it.write("overwritten text for Genesis")
                    }
                    deleteOnExit()
                }

        val toRc =
            ResourceContainerBuilder()
                .setProjectManifest(
                    listOf(
                        Project(
                            "Genesis",
                            "ufw",
                            "gen",
                            1,
                            GENESIS_FILE_PATH,
                            listOf("bible-ot"),
                        ),
                        Project(
                            "Exodus",
                            "ufw",
                            "exo",
                            2,
                            EXODUS_FILE_PATH,
                            listOf("bible-ot"),
                        ),
                    ),
                )
                .build()
                .apply {
                    addFileToContainer(sampleGenesis, GENESIS_FILE_PATH)
                    addFileToContainer(sampleExodus, EXODUS_FILE_PATH)
                }

        val fromRc =
            ResourceContainerBuilder()
                .setProjectManifest(
                    listOf(
                        Project(
                            "Genesis",
                            "ufw",
                            "gen",
                            1,
                            GENESIS_FILE_PATH,
                            listOf("bible-ot"),
                        ),
                        Project(
                            "Leviticus",
                            "ufw",
                            "lev",
                            3,
                            LEVITICUS_FILE_PATH,
                            listOf("bible-ot"),
                        ),
                    ),
                )
                .build()
                .apply {
                    addFileToContainer(overwriteGenesis, GENESIS_FILE_PATH)
                    addFileToContainer(sampleLeviticus, LEVITICUS_FILE_PATH)
                }

        @BeforeClass
        @JvmStatic
        fun mergeContainers() {
            MergeTextContent.merge(fromRc, toRc)
        }
    }

    @Test
    fun `test merge overwrites existing files`() {
        val mergedGenesis = toRc.accessor.getReader(GENESIS_FILE_PATH)
        assert(mergedGenesis.readText() == overwriteGenesis.readText())
    }

    @Test
    fun `test merge adds new files`() {
        val mergedLeviticus = toRc.accessor.getReader(LEVITICUS_FILE_PATH)
        assert(mergedLeviticus.readText() == sampleLeviticus.readText())
    }

    @Test
    fun `test merge adds new projects to manifest`() {
        val mergedManifest = toRc.manifest.projects
        assert(mergedManifest.size == 3)
        val leviticus = toRc.manifest.projects.find { it.identifier == "lev" }
        assert(leviticus != null)
        assert(leviticus!!.path == LEVITICUS_FILE_PATH)
    }

    @Test
    fun `test merge does not remove existing projects`() {
        val mergedManifest = toRc.manifest.projects
        assert(mergedManifest.size == 3)
        val genesis = toRc.manifest.projects.find { it.identifier == "gen" }
        assert(genesis != null)
        assert(genesis!!.path == GENESIS_FILE_PATH)

        val exodus = toRc.manifest.projects.find { it.identifier == "exo" }
        assert(exodus != null)
        assert(exodus!!.path == EXODUS_FILE_PATH)
    }

    @Test
    fun `test merge does not delete existing files`() {
        val mergedExodus = toRc.accessor.getReader(EXODUS_FILE_PATH)
        assert(mergedExodus.readText() == sampleExodus.readText())
    }
}
