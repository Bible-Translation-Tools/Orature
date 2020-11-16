package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.resourcecontainer.entity.*
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
    issued = LocalDate.now().toString(),
    modified = LocalDate.now().toString()
)

private fun Language.toEntity() = Language(
    identifier = slug,
    direction = direction,
    title = name
)
