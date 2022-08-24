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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class ProjectExporter {

    abstract fun export(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult>

    protected fun makeExportFilename(workbook: Workbook, metadata: ResourceMetadata): String {
        val lang = workbook.target.language.slug
        val resource = if (workbook.source.language.slug == workbook.target.language.slug) {
            metadata.identifier
        } else {
            FileNamer.DEFAULT_RC_SLUG
        }
        val project = workbook.target.slug
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return "$lang-$resource-$project-$timestamp.zip"
    }

    protected fun restoreFileExtension(file: File, extension: String) {
        val fileName = file.nameWithoutExtension + ".$extension"
        // using nio Files.move() instead of file.rename() for platform independent
        Files.move(
            file.toPath(),
            file.parentFile.resolve(fileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    protected fun setContributorInfo(
        contributors: List<Contributor>,
        metadata: ResourceMetadata,
        projectFile: File
    ) {
        ResourceContainer.load(projectFile).use { rc ->
            rc.manifest.dublinCore.apply {
                contributor = contributors.map { it.name }.toMutableList()
                creator = metadata.creator
            }
            rc.writeManifest()
        }
    }
}