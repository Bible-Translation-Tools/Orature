package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.*

import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.errors.RCException

import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.ZonedDateTime

class ImportResourceContainer(
        private val languageRepository: ILanguageRepository,
        private val metadataRepository: IResourceMetadataRepository,
        private val collectionRepository: ICollectionRepository,
        directoryProvider: IDirectoryProvider
) {

    private val rcDirectory = File(directoryProvider.getAppDataDirectory(), "rc")

    fun import(file: File) {
        when {
            file.isDirectory -> importDirectory(file)
        }
    }

    private fun importDirectory(dir: File) {
        if (validateResourceContainer(dir)) {
            if (dir.parentFile?.absolutePath != rcDirectory.absolutePath) {
                val success = dir.copyRecursively(File(rcDirectory, dir.name), true)
                if (!success) {
                    throw IOException("Could not copy resource container ${dir.name} to resource container directory")
                }
            }
            importResourceContainer(File(rcDirectory, dir.name)).subscribe { println("imported") }
        } else {
            throw RCException("Missing manifest.yaml")
        }
    }

    private fun validateResourceContainer(dir: File): Boolean {
        val names = dir.listFiles().map { it.name }
        return names.contains("manifest.yaml")
    }

    private fun importResourceContainer(container: File): Completable {
        val rc = ResourceContainer.load(container)
        val dc = rc.manifest.dublinCore

        return Completable.fromCallable {
            languageRepository.getBySlug(dc.language!!.identifier).subscribe { language ->
                val resourceMetadata = dc.mapToMetadata(container, language)
                //metadata id is going to be needed for the collection insert
                metadataRepository.insert(resourceMetadata).subscribe { id ->
                    resourceMetadata.id = id
                    for (p in rc.manifest.projects) {
                        importProject(p, resourceMetadata)
                    }
                }
            }
        }
    }

    private fun importProject(p: Project, resourceMetadata: ResourceMetadata) {
        collectionRepository.insert(p.mapToCollection(resourceMetadata.type, resourceMetadata)).subscribe()
    }
}

private fun Project.mapToCollection(type: String, metadata: ResourceMetadata): Collection {
    return Collection(
            sort,
            identifier,
            type,
            title,
            metadata
    )
}

private fun DublinCore.mapToMetadata(dir: File, lang: Language): ResourceMetadata {
    val (issuedDate, modifiedDate) = listOf(issued, modified)
            .map {
                // String could be in any of [W3 ISO8601 profile](https://www.w3.org/TR/NOTE-datetime)
                // Sanitize to be YYYY-MM-DD
                it
                        // Remove any time information
                        .substringBefore("T")
                        // Split into YYYY, MM, and DD parts
                        .split("-")
                        .toMutableList()
                        // Add any months or days to complete the YYYY-MM-DD format
                        .apply {
                            for (i in 1..(3 - size)) {
                                add("01")
                            }
                        }
                        // Combine back to a string
                        .joinToString("-")
                        // Parse to local date
                        .let { sanitized -> LocalDate.parse(sanitized) }
            }

    return ResourceMetadata(
            conformsTo,
            creator,
            description,
            format,
            identifier,
            issuedDate,
            lang,
            modifiedDate,
            publisher,
            subject,
            type,
            title,
            version,
            dir
    )
}