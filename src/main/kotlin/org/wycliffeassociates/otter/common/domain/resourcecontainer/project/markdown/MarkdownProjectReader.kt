package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project

class MarkdownProjectReader: IProjectReader {
    override fun constructProjectTree(container: ResourceContainer, project: Project): Pair<ImportResult, Tree> {
        TODO("The MarkdownProjectReader isn't yet implemented.")
    }
}
