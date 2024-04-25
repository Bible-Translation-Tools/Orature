/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import javax.inject.Inject

private const val FORMAT = "text/usfm"
private const val DEFAULT_VERSIFICATION = "ulb"

class VersificationTreeBuilder @Inject constructor(
    private val versificationRepository: IVersificationRepository
) {
    fun build(container: ResourceContainer): List<OtterTree<CollectionOrContent>>? {
        val versification = getVersification(container) ?: return null
        return constructTree(versification, container)
    }

    private fun getVersification(container: ResourceContainer): Versification? {
        val versificationCode = container.manifest.projects.firstOrNull()?.versification ?: return null
        if (versificationCode == "") return null
        return versificationRepository.getVersification(versificationCode).blockingGet()
    }

    private fun constructTree(
        versification: Versification,
        container: ResourceContainer
    ): List<OtterTree<CollectionOrContent>> {
        val projectTrees: MutableList<OtterTree<CollectionOrContent>> = mutableListOf()

        val versificationSlug = container.manifest.projects.firstOrNull()?.versification ?: DEFAULT_VERSIFICATION
        val bookSlugs = versification.getBookSlugs()
        for (book in bookSlugs) {
            val project = container.manifest.projects
                .firstOrNull {it.identifier == book}
                ?: Project(
                    title = "",
                    versification = versificationSlug,
                    identifier = book,
                    sort = Int.MAX_VALUE,
                    path = "",
                    categories = listOf(),
                )
            val projectTree = OtterTree<CollectionOrContent>(project.toCollection())
            val chapters = versification.getChaptersInBook(project.identifier)
            for (i in 1..chapters) {
                val chapterCollection = Collection(
                    sort = i,
                    slug = "${project.identifier}_${i}",
                    labelKey = ChapterLabel.of(project.identifier),
                    titleKey = "$i",
                    resourceContainer = null
                )
                val chapterTree = OtterTree<CollectionOrContent>(chapterCollection)
                val verses = versification.getVersesInChapter(project.identifier, i)

                val chapChunk = Content(
                    sort = 0,
                    labelKey = "chapter",
                    start = 1,
                    end = verses,
                    selectedTake = null,
                    text = null,
                    format = FORMAT,
                    type = ContentType.META,
                    draftNumber = 1
                )
                val chapTitle = Content(
                    sort = CHAPTER_TITLE_SORT,
                    labelKey = "chapter",
                    start = 1,
                    end = verses,
                    selectedTake = null,
                    text = null,
                    format = FORMAT,
                    type = ContentType.TITLE,
                    draftNumber = 1
                )
                if (i == 1) {
                    val bookContent = Content(
                        sort = BOOK_TITLE_SORT,
                        labelKey = "book",
                        start = 1,
                        end = verses,
                        selectedTake = null,
                        text = null,
                        format = FORMAT,
                        type = ContentType.TITLE,
                        draftNumber = 1
                    )
                    chapterTree.addChild(OtterTreeNode(bookContent))
                }

                chapterTree.addChild(OtterTreeNode(chapTitle))
                chapterTree.addChild(OtterTreeNode(chapChunk))

                for (j in 1..verses) {
                    val verseChunk = Content(
                        sort = j,
                        labelKey = "verse",
                        start = j,
                        end = j,
                        selectedTake = null,
                        text = null,
                        format = FORMAT,
                        type = ContentType.TEXT,
                        draftNumber = 1
                    )
                    chapterTree.addChild(OtterTreeNode(verseChunk))
                }
                projectTree.addChild(chapterTree)
            }
            projectTrees.add(projectTree)
        }
        return projectTrees
    }
}