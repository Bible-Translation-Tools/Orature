package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.data.model.CollectionOrContent
import org.wycliffeassociates.otter.common.data.model.MimeType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown.MarkdownProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm.UsfmProjectReader
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project

interface IProjectReader {
    /** @throws ImportException */
    fun constructProjectTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder
    ): OtterTree<CollectionOrContent>

    companion object {
        /** @throws [IllegalArgumentException] if the format type is not supported **/
        fun build(format: String, isHelp: Boolean): IProjectReader? = when (MimeType.of(format)) {
            MimeType.USFM -> {
                if (isHelp) throw ImportException(ImportResult.INVALID_RC)
                UsfmProjectReader()
            }
            MimeType.MARKDOWN -> {
                MarkdownProjectReader(isHelp)
            }
            // MimeType.of will throw an IllegalArgumentException first
            else -> throw IllegalArgumentException("Mime type $format not supported")
        }
    }
}
