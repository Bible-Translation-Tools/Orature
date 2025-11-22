package org.wycliffeassociates.otter.common.domain.project.importer

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.OtterResourceContainerConfig
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.IResourceContainerAccessor
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import org.wycliffeassociates.resourcecontainer.entity.Language
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.entity.MediaManifest
import org.wycliffeassociates.resourcecontainer.entity.MediaProject
import java.io.File

class NewSourceImporterTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private val directoryProvider = mockk<IDirectoryProvider>()
    private val resourceContainerRepository = mockk<IResourceContainerRepository>()
    private val resourceMetadataRepository = mockk<IResourceMetadataRepository>()
    private val versificationRepository = mockk<IVersificationRepository>()
    private val zipEntryTreeBuilder = mockk<IZipEntryTreeBuilder>()

    private val importer = NewSourceImporter(
        directoryProvider,
        resourceContainerRepository,
        resourceMetadataRepository,
        versificationRepository,
        zipEntryTreeBuilder
    )

    @Test
    fun `import generates USFM files when projects are missing`() {
        // Setup RC file
        val rcFile = tempDir.newFolder("test_rc")
        val manifest = Manifest(
            dublinCore = DublinCore(
                language = Language("en", "English", "ltr"),
                format = "text/usfm",
                type = "bundle",
                conformsTo = "0.2"
            ),
            projects = mutableListOf(),
            checking = org.wycliffeassociates.resourcecontainer.entity.Checking(listOf())
        )
        val mediaManifest = MediaManifest(
            projects = listOf(
                MediaProject(
                    identifier = "gen",
                    media = listOf(Media(identifier = "audio", chapterUrl = "audio/gen"))
                )
            )
        )
        ResourceContainer.create(rcFile) {
            this.manifest = manifest
            this.media = mediaManifest
            this.write()
        }

        // Mock dependencies
        val internalDir = tempDir.newFolder("internal_source")
        every { directoryProvider.getSourceContainerDirectory(any<ResourceContainer>()) } returns internalDir
        every { directoryProvider.newFileReader(any()) } returns mockk(relaxed = true)
        every { versificationRepository.getVersification("ulb") } returns Maybe.just(mockk<Versification> {
            every { getChaptersInBook("gen") } returns 1
            every { getVersesInChapter("gen", 1) } returns 1
            every { getBookSlugs() } returns listOf("gen")
        })
        every { zipEntryTreeBuilder.buildOtterFileTree(any(), any(), any()) } returns mockk<OtterTree<OtterFile>>(relaxed = true)

        val containerSlot = slot<ResourceContainer>()
        every { resourceContainerRepository.importResourceContainer(capture(containerSlot), any(), any()) } returns Single.just(ImportResult.SUCCESS)
        every { resourceContainerRepository.updateContent(any(), any()) } returns Single.just(ImportResult.SUCCESS)

        // Run import
        val result = importer.import(rcFile, null, null).blockingGet()
        if (result != ImportResult.SUCCESS) {
            throw RuntimeException("Import failed with result: $result")
        }

        // Verify
        verify(exactly = 1) { resourceContainerRepository.importResourceContainer(any(), any(), any()) }
        val importedContainer = containerSlot.captured
        Assert.assertEquals(1, importedContainer.manifest.projects.size)
        val project = importedContainer.manifest.projects.first()
        Assert.assertEquals("gen", project.identifier)
        Assert.assertEquals("ulb", project.versification)

        // Verify file content was generated in internalDir (because copyRecursively copies content to internalDir)
        val usfmFile = File(internalDir, project.path)
        Assert.assertTrue("File should exist at ${usfmFile.absolutePath}", usfmFile.exists())
        Assert.assertTrue(usfmFile.readText().contains("\\id GEN"))
    }
}
