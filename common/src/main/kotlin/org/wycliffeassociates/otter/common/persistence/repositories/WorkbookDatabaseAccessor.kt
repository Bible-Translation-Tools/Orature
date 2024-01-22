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
package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.collections.UpdateTranslation

typealias ModelTake = org.wycliffeassociates.otter.common.data.primitives.Take

interface IWorkbookDatabaseAccessors {
    fun addContentForCollection(collection: Collection, chunks: List<Content>): Completable
    fun getChildren(collection: Collection): Single<List<Collection>>
    fun getCollectionMetaContent(collection: Collection): Single<Content>
    fun getContentByCollection(collection: Collection): Single<List<Content>>
    fun getContentByCollectionActiveConnection(collection: Collection): Observable<List<Content>>
    fun updateContent(content: Content): Completable
    fun getResources(content: Content, metadata: ResourceMetadata): Observable<Content>
    fun getResources(collection: Collection, metadata: ResourceMetadata): Observable<Content>
    fun getResourceMetadata(content: Content): List<ResourceMetadata>
    fun getResourceMetadata(collection: Collection): List<ResourceMetadata>
    fun getLinkedResourceMetadata(metadata: ResourceMetadata): List<ResourceMetadata>
    fun getSubtreeResourceMetadata(collection: Collection): List<ResourceMetadata>
    fun insertTakeForContent(take: ModelTake, content: Content): Single<Int>
    fun getTakeByContent(content: Content): Single<List<ModelTake>>
    fun updateTake(take: ModelTake): Completable
    fun deleteTake(take: ModelTake, date: DateHolder): Completable
    fun getSoftDeletedTakes(metadata: ResourceMetadata, projectSlug: String): Single<List<ModelTake>>
    fun getDerivedProject(sourceCollection: Collection): Maybe<Collection>
    fun getDerivedProjects(): Single<List<Collection>>
    fun getSourceProject(targetProject: Collection): Maybe<Collection>
    fun getTranslation(sourceLanguage: Language, targetLanguage: Language): Single<Translation>
    fun updateTranslation(translation: Translation): Completable
    fun clearContentForCollection(
        chapterCollection: Collection,
        typeFilter: ContentType
    ): Single<List<ModelTake>>

    fun getChunkCount(chapterCollection: Collection): Single<Int>
}

class WorkbookDatabaseAccessor(
    private val collectionRepo: ICollectionRepository,
    private val contentRepo: IContentRepository,
    private val resourceRepo: IResourceRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository,
    private val takeRepo: ITakeRepository,
    private val languageRepo: ILanguageRepository,
    private val updateTranslationUseCase: UpdateTranslation
) : IWorkbookDatabaseAccessors {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getChunkCount(chapterCollection: Collection): Single<Int> {
        return contentRepo
            .getByCollection(chapterCollection)
            .map { it.count { it.type == ContentType.TEXT } }
    }

    override fun clearContentForCollection(
        chapterCollection: Collection,
        typeFilter: ContentType
    ): Single<List<ModelTake>> {
        return takeRepo
            .getByCollection(chapterCollection, true)
            .map {
                it.filter { take ->
                    takeRepo.getContentType(take).blockingGet() == typeFilter
                }
            }
            .map {
                it.forEach {
                    takeRepo.delete(it).blockingAwait()
                }
                takeRepo.deleteExpiredTakes().blockingAwait()
                contentRepo.deleteForCollection(chapterCollection, typeFilter).blockingAwait()
                it
            }
    }

    override fun addContentForCollection(collection: Collection, chunks: List<Content>): Completable {
        return Observable.just(chunks)
            .map { contents ->
                contentRepo.insertForCollection(contents, collection)
                    .blockingGet()
            }
            .flatMapCompletable { contents ->
                val sourceContents = collectionRepo.getSource(collection)
                    .blockingGet()
                    ?.let { collection ->
                        contentRepo.getByCollection(collection).blockingGet()
                    }

                if (sourceContents == null || sourceContents.isEmpty()) {
                    Completable.complete()
                } else {
                    contentRepo.linkDerivedToSource(contents, sourceContents)
                }
            }
    }

    override fun getChildren(collection: Collection) = collectionRepo.getChildren(collection)

    override fun getCollectionMetaContent(collection: Collection) = contentRepo.getCollectionMetaContent(collection)
    override fun getContentByCollection(collection: Collection) = contentRepo.getByCollection(collection)
    override fun getContentByCollectionActiveConnection(collection: Collection): Observable<List<Content>> {
        return contentRepo.getByCollectionWithPersistentConnection(collection)
    }

    override fun updateContent(content: Content) = contentRepo.update(content)

    override fun getResources(content: Content, metadata: ResourceMetadata) =
        resourceRepo.getResources(content, metadata)

    override fun getResources(collection: Collection, metadata: ResourceMetadata) =
        resourceRepo.getResources(collection, metadata)

    override fun getResourceMetadata(content: Content) = resourceRepo.getResourceMetadata(content)
    override fun getResourceMetadata(collection: Collection) = resourceRepo.getResourceMetadata(collection)

    override fun getLinkedResourceMetadata(metadata: ResourceMetadata) =
        resourceMetadataRepo.getLinked(metadata).blockingGet()

    override fun getSubtreeResourceMetadata(collection: Collection) =
        resourceRepo.getSubtreeResourceMetadata(collection)

    override fun insertTakeForContent(take: ModelTake, content: Content) = takeRepo.insertForContent(take, content)
    override fun getTakeByContent(content: Content) = takeRepo.getByContent(content, includeDeleted = true)
    override fun updateTake(take: ModelTake) = takeRepo.update(take)
    override fun deleteTake(take: ModelTake, date: DateHolder) = takeRepo.update(take.copy(deleted = date.value))

    override fun getSoftDeletedTakes(metadata: ResourceMetadata, projectSlug: String) =
        takeRepo.getSoftDeletedTakes(collectionRepo.getProjectBySlugAndMetadata(projectSlug, metadata).blockingGet())

    override fun getDerivedProject(sourceCollection: Collection): Maybe<Collection> {
        return collectionRepo.getDerivedProject(sourceCollection)
    }

    override fun getDerivedProjects(): Single<List<Collection>> = collectionRepo.getDerivedProjects()

    override fun getSourceProject(targetProject: Collection): Maybe<Collection> =
        collectionRepo.getSource(targetProject)

    override fun getTranslation(sourceLanguage: Language, targetLanguage: Language): Single<Translation> {
        return languageRepo.getTranslation(sourceLanguage, targetLanguage)
    }

    override fun updateTranslation(translation: Translation): Completable {
        return updateTranslationUseCase.update(translation)
    }
}