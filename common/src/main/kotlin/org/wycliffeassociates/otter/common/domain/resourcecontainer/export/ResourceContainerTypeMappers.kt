package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.resourcecontainer.entity.*
import java.time.LocalDate

fun buildManifest(
    metadata: ResourceMetadata,
    book: Book,
    projectPath: String
): Manifest {
    val dublinCore = metadata.toEntity()
    val project = book.toEntity(projectPath)
    return Manifest(dublinCore, listOf(project), Checking())
}

private fun Book.toEntity(projectPath: String, sort: Int = 1) = Project(
    title = title,
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
    type = type,
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
