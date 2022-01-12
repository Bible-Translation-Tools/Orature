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

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.time.LocalDate

fun buildManifest(
    metadata: ResourceMetadata,
    workbook: Workbook,
    projectPath: String
): Manifest {
    val dublinCoreSource = with(workbook.source.resourceMetadata) { Source(identifier, language.slug, version) }
    val dublinCore = metadata.toEntity().apply { source.add(dublinCoreSource) }
    val project = workbook.target.toEntity(projectPath)
    return Manifest(dublinCore, listOf(project), Checking())
}

fun buildManifest(
    targetMetadata: ResourceMetadata,
    sourceMetadata: ResourceMetadata,
    targetProject: Collection,
    projectPath: String
): Manifest {
    val dublinCoreSource = with(sourceMetadata) { Source(identifier, language.slug, version) }
    val dublinCore = targetMetadata.toEntity().apply { source.add(dublinCoreSource) }
    val project = targetProject.toEntity(projectPath)
    return Manifest(dublinCore, listOf(project), Checking())
}

private fun Book.toEntity(projectPath: String, sort: Int = 1) = Project(
    title = title,
    identifier = slug,
    sort = sort,
    path = projectPath
)

private fun Collection.toEntity(projectPath: String, sort: Int = 1) = Project(
    title = titleKey,
    identifier = slug,
    sort = sort,
    path = projectPath
)

private fun ResourceMetadata.toEntity() = DublinCore(
    title = title,
    identifier = identifier,
    version = version,
    subject = subject,
    creator = creator,
    type = type.slug,
    format = format,
    language = language.toEntity(),
    rights = license,
    issued = LocalDate.now().toString(),
    modified = LocalDate.now().toString()
)

private fun Language.toEntity() = org.wycliffeassociates.resourcecontainer.entity.Language(
    identifier = slug,
    direction = direction,
    title = name
)
