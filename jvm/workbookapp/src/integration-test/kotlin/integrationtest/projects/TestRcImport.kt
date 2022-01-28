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
package integrationtest.projects

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Assert.assertEquals
import javax.inject.Inject
import javax.inject.Provider
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType.BODY
import org.wycliffeassociates.otter.common.data.primitives.ContentType.META
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TEXT
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TITLE
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class TestRcImport {

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun ulb() {
        dbEnvProvider.get()
            .import("en_ulb.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        TEXT to 31104,
                        META to 1189
                    ),
                    collections = 1256,
                    links = 0
                )
            )
    }

    /**
     * Runs the same test as ulb(), but rather than test the provided and tested ulb resource container,
     * we instead test the version downloaded from WACS through the downloadUlb gradle task. Failure of this
     * test while succeeding the ulb() test therefore implies either potential issues in the WACS repository
     * or a failure to download the content.
     */
    @Test
    fun ulbFromWacs() {
        dbEnvProvider.get()
            .import("en_ulb.zip", true)
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        TEXT to 31102,
                        META to 1189
                    ),
                    collections = 1256,
                    links = 0
                )
            )
    }

    @Test
    fun ulbAndHelps() {
        dbEnvProvider.get()
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .assertRowCounts(
                message = "Row counts after importing TN",
                expected = RowCount(
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31104,
                        TITLE to 80148,
                        BODY to 77433
                    ),
                    collections = 1256,
                    links = 157581
                )
            )
            .import("en_tq-v19-10.zip")
            .assertRowCounts(
                message = "Row counts after importing TQ",
                expected = RowCount(
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31104,
                        TITLE to 98520,
                        BODY to 95805
                    ),
                    collections = 1256,
                    links = 194325
                )
            )
    }

    @Test
    fun obsV6() {
        dbEnvProvider.get()
            .import("obs-biel-v6.zip")
            .assertRowCounts(
                RowCount(
                    collections = 57,
                    contents = mapOf(
                        META to 55,
                        TEXT to 716
                    ),
                    links = 0
                )
            )
    }

    @Test
    fun obsAndTnV6() {
        dbEnvProvider.get()
            .import("obs-biel-v6.zip")
            .import("obs-tn-biel-v6.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 55,
                        TEXT to 716,
                        TITLE to 2237,
                        BODY to 2237
                    ),
                    collections = 57,
                    links = 4474
                )
            )
    }

    @Test
    fun obsSlugs() {
        dbEnvProvider.get()
            .import("obs-biel-v6.zip")
            .assertSlugs(
                "obs",
                CollectionDescriptor(label = "book", slug = "obs"),
                CollectionDescriptor(label = "project", slug = "obs"),
                CollectionDescriptor(label = "chapter", slug = "obs_1")
            )
    }

    @Test
    fun ulbSlugs() {
        dbEnvProvider.get()
            .import("en_ulb.zip")
            .assertSlugs(
                "ulb",
                CollectionDescriptor(label = "bundle", slug = "ulb"),
                CollectionDescriptor(label = "project", slug = "gen"),
                CollectionDescriptor(label = "chapter", slug = "gen_1")
            )
    }

    @Test
    fun ulbVerseCount() {
        val books = javaClass.getResource("/verse-count-en_ulb-v21-05/books.txt").readText().split("\n")
        val tests = mutableListOf<ChapterVerse>()
        for (book in books) {
            val csv = javaClass.getResource("/verse-count-en_ulb-v21-05/$book.csv").readText()
            val mapper = CsvMapper().registerModule(KotlinModule())
            val schema = CsvSchema.builder().addColumn("Chapter").addColumn("Verses").setUseHeader(true).build()
            val reader: MappingIterator<ChapterVerse> = mapper
                .readerFor(ChapterVerse::class.java)
                .with(schema)
                .readValues(csv)
            val data = reader.readAll()

            for (test in data) {
                test.chapter = "${book}_${test.chapter}"
            }
            tests.addAll(data)
        }

        dbEnvProvider.get()
            .import("en_ulb.zip", true)
            .assertChapters("ulb", *tests.toTypedArray())
    }


    @Test
    fun `import override when existing rc has different version`() {
        val oldRCVer = "12"
        val newRCVer = "999"

        dbEnvProvider.get()
            .import("en_ulb.zip")
            .assertRowCounts(
                RowCount(
                    collections = 1256,
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31104
                    )
                )
            )
            .apply {
                assertEquals(
                    oldRCVer,
                    db.resourceMetadataDao.fetchAll().single().version
                )
            }
            .import("en_ulb_newer_ver.zip")
            .assertRowCounts(
                RowCount(
                    collections = 1,
                    contents = mapOf(),
                    links = 0
                )
            )
            .apply {
                assertEquals(
                    newRCVer,
                    db.resourceMetadataDao.fetchAll().single().version
                )
            }
    }
}
