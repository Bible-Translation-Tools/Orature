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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.reactivex.Single
import jooq.Tables.CONTENT_DERIVATIVE
import org.junit.Assert
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class DatabaseEnvironment @Inject constructor(
    val db: AppDatabase,
    val directoryProvider: IDirectoryProvider,
    val importRcFactory: RCImporterFactory,
    val createProjectProvider: Provider<CreateProject>,
    val importLanguagesProvider: Provider<ImportLanguages>
) {
    init {
        setUpDatabase()
    }

    private val cb = object : ProjectImporterCallback {
        override fun onRequestUserInput(): Single<ImportOptions> {
            TODO("Not yet implemented")
        }

        override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
            TODO("Not yet implemented")
        }

        override fun onError() {
            TODO("Not yet implemented")
        }
    }

    private val importer
        get() = importRcFactory.makeImporter()

    fun import(rcFile: String, importAsStream: Boolean = false, unzip: Boolean = false): DatabaseEnvironment {
        if (importAsStream) {
            val tempFile = directoryProvider.createTempFile("db-env-import-test", ".zip")
            rcResourceStream(rcFile).use { input ->
                tempFile.outputStream().use { output ->
                    input.transferTo(output)
                }
            }
            val result = importer.import(tempFile, cb).blockingGet()
            Assert.assertEquals(
                ImportResult.SUCCESS,
                result
            )
            return this
        }

        val resourceFile = if (unzip) {
            unzipProject(rcFile)
        } else {
            rcResourceFile(rcFile)
        }

        val result =importer.import(resourceFile, cb).blockingGet()
        Assert.assertEquals(
            ImportResult.SUCCESS,
            result
        )
        return this
    }

    fun createProject(sourceProject: Collection, targetLanguage: Language): Collection =
        createProjectProvider.get()
            .create(sourceProject, targetLanguage)
            .blockingGet()

    fun unzipProject(rcFile: String, dir: File? = null): File {
        val targetDir = dir ?: createTempDir("orature_unzip")
        directoryProvider
            .newFileReader(rcResourceFile(rcFile))
            .use { fileReader ->
                fileReader.copyDirectory("/", targetDir)
            }
        return targetDir
    }

    fun assertRowCounts(expected: RowCount, message: String? = null): DatabaseEnvironment {
        val actual = RowCount(
            // These ?.let constructs let us skip comparing counts that aren't specified in [expected].
            contents = expected.contents?.let { _ -> fetchContentRowCount() },
            collections = expected.collections?.let { _ -> fetchCollectionRowCount() },
            links = expected.links?.let { _ -> fetchLinkRowCount() },
            derivatives = expected.derivatives?.let { _ -> fetchDerivativeRowCount() }
        )
        Assert.assertEquals(message, expected, actual)
        return this
    }

    fun assertSlugs(
        rcSlug: String,
        vararg collectionSlug: CollectionDescriptor
    ): DatabaseEnvironment {
        val rc = db.resourceMetadataDao.fetchAll().firstOrNull { it.identifier == rcSlug }
        Assert.assertNotNull("Retrieving resource container info", rc)

        collectionSlug.forEach { (label, slug) ->
            val entity = db.collectionDao.fetch(containerId = rc!!.id, label = label, slug = slug)
            Assert.assertNotNull("Retrieving $label $slug", entity)
        }

        return this
    }

    fun assertChapters(
        rcSlug: String,
        vararg chapter: ChapterVerse
    ): DatabaseEnvironment {
        val rc = db.resourceMetadataDao.fetchAll().firstOrNull { it.identifier == rcSlug }
        Assert.assertNotNull("Retrieving resource container info", rc)

        chapter.forEach { (slug, verseCount) ->
            val entity = db.collectionDao.fetch(containerId = rc!!.id, label = "chapter", slug = slug)
            Assert.assertNotNull("Retrieving chapter $slug", entity)
            val content = db.contentDao.fetchByCollectionId(entity!!.id)
            val verses = content.filter { it.type_fk == 1 }.count()
            val meta = content.filter { it.type_fk == 2 }.count()
            Assert.assertEquals("Verses for $slug", verseCount, verses)
            Assert.assertEquals("Meta for $slug", 1, meta)
        }

        return this
    }

    private fun setUpDatabase() {
        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
        importLanguagesProvider.get()
            .import(langNames)
            .onErrorComplete()
            .blockingAwait()
    }

    private fun rcResourceFile(rcFile: String) =
        File(
            TestRcImport::class.java.classLoader
                .getResource("resource-containers/$rcFile")!!
                .toURI()
                .path
        )

    /**
     * The path here should match that of the resource structure of main
     */
    private fun rcResourceStream(rcFile: String) =
        TestRcImport::class.java.classLoader
            .getResourceAsStream("content/$rcFile")!!

    private fun fetchCollectionRowCount() = db.collectionDao.fetchAll().count()
    private fun fetchLinkRowCount() = db.resourceLinkDao.fetchAll().count()
    private fun fetchDerivativeRowCount() = db.dsl.selectCount().from(CONTENT_DERIVATIVE).fetchOne(0) as Int
    private fun fetchContentRowCount() =
        db.contentDao.fetchAll()
            .groupBy { it.type_fk }
            .mapValues { it.value.count() }
            .mapKeys { db.contentTypeDao.fetchForId(it.key)!! }
}

data class CollectionDescriptor(
    val label: String,
    val slug: String
)

@JsonPropertyOrder("chapter, verses")
data class ChapterVerse(
    @JsonProperty("Chapter") var chapter: String,
    @JsonProperty("Verses") val verses: Int
)
