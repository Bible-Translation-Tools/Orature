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
package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.flatMapIterable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import javax.inject.Inject

class CreateProject @Inject constructor(
    private val collectionRepo: ICollectionRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository
) {

    @Inject
    lateinit var translationCreation: CreateTranslation

    /**
     * Create derived collections for each source RC that has content in sourceProject's subtree, optionally
     * limited to resourceId (if not null).
     *
     * @param sourceProject The Book Collection to derive from
     * @param targetLanguage The language of the derived project
     * @param resourceId Filters the source and linked RCs by this optional filter
     * @param deriveProjectFromVerses Derives Content/Chunks for Chapters based on the Verses parsed in the text.
     */
    fun create(
        sourceProject: Collection,
        targetLanguage: Language,
        mode: ProjectMode? = null,
        resourceId: String? = null,
        deriveProjectFromVerses: Boolean = false
    ): Single<Collection> {
        // Find the source RC and its linked (help) RCs
        val sourceRc = sourceProject.resourceContainer
            ?: throw NullPointerException("Source project has no metadata")
        val sourceLinkedRcs = resourceMetadataRepo.getLinked(sourceRc)
            .toObservable()
            .flatMapIterable()
        val sourceAndLinkedRcs = sourceLinkedRcs.startWith(sourceRc)

        // If a resourceId filter is requested, apply it.
        val matchingRcs = when (resourceId) {
            null -> sourceAndLinkedRcs
            else -> sourceAndLinkedRcs.filter { resourceId == it.identifier }
        }

        val projectMode = when {
            mode != null -> mode
            sourceProject.resourceContainer?.language == targetLanguage -> {
                ProjectMode.NARRATION
            }
            else -> {
                ProjectMode.TRANSLATION
            }
        }

        // Create derived projects for each of the sources
        return matchingRcs
            .toList()
            .flatMap {
                collectionRepo.deriveProject(it, sourceProject, targetLanguage, deriveProjectFromVerses, projectMode)
            }
    }

    fun createAllBooks(
        sourceLanguage: Language,
        targetLanguage: Language,
        projectMode: ProjectMode,
        resourceId: String? = null
    ): Completable {
        val isVerseByVerse = projectMode != ProjectMode.TRANSLATION
        return collectionRepo.getRootSources()
            .flattenAsObservable {
                it
            }
            .filter { collection ->
                collection.resourceContainer?.language == sourceLanguage &&
                        (resourceId?.let  { collection.resourceContainer?.identifier == resourceId } ?: true)
            }
            .firstOrError()
            .flatMap { rootCollection ->
                collectionRepo
                    .deriveProjects(
                        rootCollection,
                        targetLanguage,
                        isVerseByVerse,
                        projectMode
                    )
            }
            .subscribeOn(Schedulers.io())
            .ignoreElement()
            .concatWith(
                translationCreation.create(sourceLanguage, targetLanguage).ignoreElement()
            )
    }
}
