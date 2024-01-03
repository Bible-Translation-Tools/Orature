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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.otterConfigCategories
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown.MarkdownProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm.UsfmProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project

interface IProjectReader {
    /**
     * Constructs a tree of CollectionOrContent from the given project.
     *
     * @throws ImportException */
    fun constructProjectTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder,
    ): OtterTree<CollectionOrContent>

    companion object {
        /** @throws [IllegalArgumentException] if the format type is not supported **/
        fun build(
            format: String,
            isHelp: Boolean,
        ): IProjectReader =
            when (MimeType.of(format)) {
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

        /**
         * Constructs a tree of CollectionOrContent from all projects in the container
         *
         * @throws ImportException */
        fun constructContainerTree(
            container: ResourceContainer,
            zipEntryTreeBuilder: IZipEntryTreeBuilder,
        ): OtterTree<CollectionOrContent> {
            val projectReader =
                try {
                    build(
                        format = container.manifest.dublinCore.format,
                        isHelp = ContainerType.of(container.manifest.dublinCore.type) == ContainerType.Help,
                    )
                } catch (e: IllegalArgumentException) {
                    null
                } ?: throw ImportException(ImportResult.UNSUPPORTED_CONTENT)

            val root = OtterTree<CollectionOrContent>(container.toCollection())
            val categoryInfo = container.otterConfigCategories()
            for (project in container.manifest.projects) {
                var parent = root
                for (categorySlug in project.categories) {
                    // use the `latest` RC spec to treat categories as hierarchical
                    // look for a matching category under the parent
                    val existingCategory =
                        parent.children
                            .map { it as? OtterTree<CollectionOrContent> }
                            .firstOrNull { (it?.value as? Collection)?.slug == categorySlug }
                    parent =
                        if (existingCategory != null) {
                            existingCategory
                        } else {
                            // category node does not yet exist
                            val category =
                                categoryInfo
                                    .firstOrNull { it.identifier == categorySlug }
                                    ?: continue
                            val categoryNode = OtterTree<CollectionOrContent>(category.toCollection())
                            parent.addChild(categoryNode)
                            categoryNode
                        }
                }
                val projectTree =
                    projectReader
                        .constructProjectTree(
                            container,
                            project,
                            zipEntryTreeBuilder,
                        )
                parent.addChild(projectTree)
            }
            return root
        }
    }
}
