package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.dao.LanguageDao
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata

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
        private val languageDao: LanguageDao,
        private val metadataDao: Dao<ResourceMetadata>,
        private val collectionDao: Dao<Collection>,
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
            importResourceContainer(File(rcDirectory, dir.name))
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
            languageDao.getBySlug(dc.language.identifier).subscribe {
                val resourceMetadata = dc.mapToMetadata(container, it)
                //set the id in the resourceMetadata object once it returns from the insert call
                //metadata id is going to be needed for the collection insert
                metadataDao.insert(resourceMetadata).subscribe {
                    resourceMetadata.id = it
                    for (p in rc.manifest.projects) {
                        importProject(p, resourceMetadata)
                    }
                }
            }
        }
    }

    private fun importProject(p: Project, resourceMetadata: ResourceMetadata) {
        collectionDao.insert(p.mapToCollection(resourceMetadata.type, resourceMetadata))
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
    return ResourceMetadata(
            conformsTo,
            creator,
            description,
            format,
            identifier,
            ZonedDateTime.parse(issued),
            lang,
            ZonedDateTime.parse(modified),
            publisher,
            subject,
            type,
            title,
            version,
            dir
    )
}