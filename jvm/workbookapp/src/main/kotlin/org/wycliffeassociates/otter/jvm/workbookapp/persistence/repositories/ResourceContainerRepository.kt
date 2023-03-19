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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.jooq.DSLContext
import org.jooq.Record3
import org.jooq.Select
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ResourceMetadataMapper
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.time.LocalDateTime

class ResourceContainerRepository @Inject constructor(
    private val database: AppDatabase,
    private val collectionRepository: ICollectionRepository,
    private val contentRepository: IContentRepository,
    private val resourceRepository: IResourceRepository,
    private val resourceMetadataRepository: IResourceMetadataRepository
) : IResourceContainerRepository {
    private val logger = LoggerFactory.getLogger(ResourceContainerRepository::class.java)

    private val collectionDao = database.collectionDao
    private val contentDao = database.contentDao
    private val contentTypeDao = database.contentTypeDao
    private val resourceMetadataDao = database.resourceMetadataDao
    private val languageDao = database.languageDao
    private val resourceLinkDao = database.resourceLinkDao
    private val contentMapper = ContentMapper(contentTypeDao)

    override fun importResourceContainer(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>,
        languageSlug: String
    ): Single<ImportResult> {
        val dublinCore = rc.manifest.dublinCore
        return Completable
            .fromAction {
                database.transaction { dsl ->
                    val languageEntity = languageDao.fetchBySlug(languageSlug, dsl)
                        ?: throw NullPointerException("Could not find language with slug $languageSlug.")
                    val language = LanguageMapper().mapFromEntity(languageEntity)
                    val metadata = dublinCore.mapToMetadata(rc.file, language)
                        .let {
                            insertMetadataOrThrow(dsl, it)
                        }

                    val relatedDublinCoreIds: List<Int> =
                        linkRelatedResourceContainers(metadata, dublinCore.relation, dublinCore.creator, dsl)

                    if (ContainerType.of(rc.type()) == ContainerType.Help) {
                        if (relatedDublinCoreIds.isEmpty()) {
                            logger.error("Unmatched help for ${rc.manifest.dublinCore.identifier}")
                            throw ImportException(ImportResult.UNMATCHED_HELP)
                        }
                        relatedDublinCoreIds.forEach { relatedId ->
                            val ih = ImportHelper(metadata, relatedId, dsl)
                            ih.import(rcTree)
                        }
                    } else {
                        val ih = ImportHelper(metadata, null, dsl)
                        ih.import(rcTree)
                    }
                }
            }
            .toSingleDefault(ImportResult.SUCCESS)
            .onErrorReturn { e ->
                logger.error("Error in importResourceContainer for rc: $rc, language: $languageSlug", e)
                e.castOrFindImportException()?.result ?: ImportResult.LOAD_RC_ERROR
            }
            .doFinally { rc.close() }
            .subscribeOn(Schedulers.io())
    }

    override fun updateContent(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>,
        languageSlug: String
    ): Single<ImportResult> {
        return Single.fromCallable {
            val rcMetadata = getMetadataForContainer(rc) ?: run {
                return@fromCallable ImportResult.IMPORT_ERROR
            }
            val projects = collectionRepository
                .getSourceProjects()
                .map { it.filter { it.resourceContainer == rcMetadata } }
                .blockingGet()

            val databaseMap = hashMapOf<Collection, MutableList<Content>>()

            projects
                .map { getProjectAsOtterTree(it, rcMetadata).toChapterContentMap() }
                .forEach { databaseMap.putAll(it) }

            val parsedTextMap = rcTree.toChapterContentMap()
            updateTextContent(databaseMap, parsedTextMap)
            updateBridges(databaseMap, parsedTextMap)

            return@fromCallable ImportResult.SUCCESS
        }.subscribeOn(Schedulers.io())
    }

    override fun updateCollections(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>,
        languageSlug: String
    ): Single<ImportResult> {
        return Single
            .fromCallable {
                val rcMetadata = getMetadataForContainer(rc) ?: run {
                    return@fromCallable ImportResult.IMPORT_ERROR
                }

                val derivedMetadata = resourceMetadataRepository
                    .getAllDerivatives(rcMetadata)
                    .blockingGet()

                val sourceProjects = collectionRepository
                    .getSourceProjects()
                    .map { it.filter { it.resourceContainer == rcMetadata } }
                    .blockingGet()

                val derivativeProjects = collectionRepository
                    .getDerivedProjects()
                    .map { it.filter { it.resourceContainer in derivedMetadata } }
                    .blockingGet()

                val updateFrom = rcTree.toCollectionList()
                updateCollections(
                    listOf(*sourceProjects.toTypedArray(), *derivativeProjects.toTypedArray()),
                    updateFrom
                )

                return@fromCallable ImportResult.SUCCESS
            }
            .onErrorReturn { ImportResult.LOAD_RC_ERROR }
            .subscribeOn(Schedulers.io())
    }

    private fun updateCollections(
        updateTo: List<Collection>,
        updateFrom: List<Collection>,
    ) {
        updateFrom.forEach { updatedCollection ->
            val toUpdate = updateTo.filter {
                it.slug == updatedCollection.slug && (it.titleKey.isNullOrEmpty() || it.sort == Int.MAX_VALUE)
            }
            toUpdate.forEach { match ->
                val copy = match.copy(
                    sort = if (updatedCollection.sort != Int.MAX_VALUE) updatedCollection.sort else match.sort,
                    titleKey = updatedCollection.titleKey.ifBlank { match.titleKey },
                    labelKey = updatedCollection.labelKey
                )
                logger.info("Updating collection of title: ${copy.titleKey} to updated title: ${updatedCollection.titleKey}")
                try {
                    collectionRepository.update(copy).blockingGet()
                } catch (e: Exception) {
                    logger.error("Error updating collection: $copy", e)
                }
            }
        }
    }

    private fun updateTextContent(
        databaseMap: Map<Collection, List<Content>>,
        parsedTextMap: Map<Collection, List<Content>>
    ) {
        parsedTextMap.keys.forEach { collection ->
            val textMapContent = parsedTextMap[collection]
            val matchingCollection = databaseMap.keys.find { it.slug == collection.slug } ?: return@forEach
            val databaseContent = databaseMap[matchingCollection]!!

            textMapContent!!.forEach { content ->
                val match = databaseContent.find { it.start == content.start && it.type == content.type }
                match?.let {
                    match.text = content.text ?: ""
                    contentRepository.update(match).blockingGet()
                }
            }
        }
    }

    private fun updateBridges(
        databaseMap: Map<Collection, List<Content>>,
        parsedTextMap: Map<Collection, List<Content>>
    ) {
        parsedTextMap.keys.forEach { collection ->
            val textMapContent = parsedTextMap[collection]
            val matchingCollection = databaseMap.keys.find { it.slug == collection.slug } ?: return@forEach
            val databaseContent = databaseMap[matchingCollection]!!

            textMapContent!!.forEach { content ->
                val match = databaseContent.find { it.start == content.start && it.type == content.type }
                match?.let {
                    if (match.end != content.end && match.type != ContentType.META) {
                        match.end = content.end
                        logger.info("Bridging ${collection.slug}:${match.start}-${match.end}")
                        contentRepository.update(match).blockingGet()
                        for (i in (match.start + 1)..match.end) {
                            val found = databaseContent.find { it.start == i && it.type != ContentType.META }
                            if (found != null) {
                                found.bridged = true
                                contentRepository.update(found).blockingGet()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getProjectAsOtterTree(book: Collection, rc: ResourceMetadata): OtterTree<CollectionOrContent> {
        val tree = OtterTree<CollectionOrContent>(book)
        val chapters = collectionDao.fetchChildren(CollectionMapper().mapToEntity(book))
        chapters.forEach { collectionEntity ->
            val collection = CollectionMapper().mapFromEntity(collectionEntity, rc)
            val chapterTree = OtterTree<CollectionOrContent>(collection)
            val content = contentRepository.getByCollection(collection).blockingGet()
            content.forEach {
                val node = OtterTreeNode<CollectionOrContent>(it)
                chapterTree.addChild(node)
            }
            tree.addChild(chapterTree)
        }
        return tree
    }

    fun OtterTree<CollectionOrContent>.toCollectionList(): List<Collection> {
        if (value is Content) {
            return mutableListOf()
        } else {
            val list = mutableListOf(value as Collection)
            children.forEach {
                list.addAll((it as? OtterTree<CollectionOrContent>)?.toCollectionList() ?: mutableListOf())
            }
            return list
        }
    }

    fun OtterTree<CollectionOrContent>.toChapterContentMap(): Map<Collection, MutableList<Content>> {
        val isBook = children.any { it.hasContent() }
        return if (isBook) {
            toChapterContentMapFromBook()
        } else {
            toChapterContentMapFromResource()
        }
    }

    private fun OtterTreeNode<CollectionOrContent>.hasContent(): Boolean {
        if (value is Collection) {
            return (this as OtterTree<CollectionOrContent>).children.any { it.value is Content }
        } else {
            return false
        }
    }

    private fun OtterTree<CollectionOrContent>.toChapterContentMapFromResource(): Map<Collection, MutableList<Content>> {
        val bigMap = hashMapOf<Collection, MutableList<Content>>()

        val trees = children.map { it as OtterTree<CollectionOrContent> }
        val maps = trees.map { it.toChapterContentMap() }
        maps.forEach {
            bigMap.putAll(it)
        }
        return bigMap
    }

    private fun OtterTree<CollectionOrContent>.toChapterContentMapFromBook(): Map<Collection, MutableList<Content>> {
        val isBook = children.any { it.hasContent() }

        val bigMap = hashMapOf<Collection, MutableList<Content>>()
        if (isBook) {
            for (child in children) {
                val collection = child.value
                if (collection !is Collection) {
                    continue
                }
                val content = (child as OtterTree<CollectionOrContent>).children.map { it.value }.map { it as Content }
                bigMap[collection] = content.toMutableList()
            }
        }
        return bigMap
    }

    private fun getMetadataForContainer(rc: ResourceContainer): ResourceMetadata? {
        val language = languageDao.fetchBySlug(rc.manifest.dublinCore.language.identifier) ?: run {
            return null
        }
        val metadata = resourceMetadataDao
            .fetchAll()
            .firstOrNull { entity ->
                val isSource = entity.derivedFromFk == null
                val rcSlugMatches = entity.identifier == rc.manifest.dublinCore.identifier
                val languagesMatch = entity.languageFk == language.id
                isSource && rcSlugMatches && languagesMatch
            } ?: run { return null }

        return ResourceMetadataMapper()
            .mapFromEntity(
                metadata,
                LanguageMapper().mapFromEntity(language)
            )
    }

    /**
     * Insert metadata, return metadata modified to include row ID.
     * @throws [ImportException] if a matching row already exists.
     */
    private fun insertMetadataOrThrow(
        dsl: DSLContext,
        metadata: ResourceMetadata
    ): ResourceMetadata {
        val existingRow = resourceMetadataDao.fetchLatestVersion(metadata.language.slug, metadata.identifier)
        if (existingRow != null && existingRow.derivedFromFk == null) {
            logger.error("Error in inserting metadata, row already exists!: $existingRow")
            throw ImportException(ImportResult.ALREADY_EXISTS)
        }
        val entity = ResourceMetadataMapper().mapToEntity(metadata)
        val rowId = resourceMetadataDao.insert(entity, dsl)
        return metadata.copy(id = rowId)
    }

    private fun linkRelatedResourceContainers(
        newDublinCore: ResourceMetadata,
        relations: List<String>,
        creator: String,
        dsl: DSLContext
    ): List<Int> {
        val relatedIds = mutableListOf<Int>()
        relations.forEach { relation ->
            val (languageSlug, identifier) = relation.split('/')
            resourceMetadataDao.fetchLatestVersion(
                languageSlug = languageSlug,
                identifier = identifier,
                creator = creator,
                relaxCreatorIfNoMatch = true,
                derivedFromFk = null, // derivedFromFk=null since we are looking for the original resource container
                dsl = dsl
            )
                ?.let { relatedDublinCore ->
                    resourceMetadataDao.addLink(newDublinCore.id, relatedDublinCore.id, dsl)
                    relatedIds.add(relatedDublinCore.id)
                }
        }
        return relatedIds
    }

    override fun removeResourceContainer(
        resourceContainer: ResourceContainer
    ): Single<DeleteResult> {
        return Single.fromCallable {
            var result = DeleteResult.SUCCESS

            database.transaction { dsl ->
                val metadataEntity = resourceMetadataDao.fetchLatestVersion(
                    resourceContainer.manifest.dublinCore.language.identifier,
                    resourceContainer.manifest.dublinCore.identifier,
                    resourceContainer.manifest.dublinCore.creator,
                    derivedFromFk = null,
                    dsl = dsl
                )

                val derivedRcExists = resourceMetadataDao.fetchAll().any {
                    it.derivedFromFk != null && it.derivedFromFk == metadataEntity?.id
                }

                when {
                    metadataEntity == null -> {
                        result = DeleteResult.NOT_FOUND
                        return@transaction
                    }

                    derivedRcExists -> {
                        result = DeleteResult.DEPENDENCY_EXISTS
                        return@transaction
                    }

                    else -> metadataEntity!!
                }

                // delete entities with foreign keys refer to rc first
                collectionDao.fetchAll(dsl)
                    .filter { it.dublinCoreFk == metadataEntity.id }
                    .forEach {
                        collectionDao.delete(it, dsl)
                    }

                resourceMetadataDao.delete(metadataEntity, dsl)
            }

            result
        }
            .doOnError { e ->
                logger.error("Error in removeResourceContainer.", e)
            }
            .doFinally { resourceContainer.close() }
            .subscribeOn(Schedulers.io())
    }

    private fun findRootCollectionsForRc(dublinCoreId: Int): List<Collection> {
        return collectionRepository
            .getRootSources()
            .blockingGet()
            .filter { it.resourceContainer?.id == dublinCoreId }
    }

    inner class ImportHelper(
        private val metadata: ResourceMetadata,
        private val relatedBundleDublinCoreId: Int?,
        private val dsl: DSLContext
    ) {
        private val dublinCoreIdDslVal = DSL.`val`(metadata.id)

        fun import(node: OtterTreeNode<CollectionOrContent>) {
            importCollection(null, node)

            relatedBundleDublinCoreId
                ?.let(::findRootCollectionsForRc)
                ?.map { it.id }
                ?.forEach(resourceRepository::calculateAndSetSubtreeHasResources)
        }

        /** Finds a collection from the database that matches the given collection on slug, label, and containerId. */
        private fun fetchCollectionFromDb(collection: Collection, containerId: Int): Collection? {
            val entity = collectionDao.fetch(
                slug = collection.slug,
                label = collection.labelKey,
                containerId = containerId
            )

            return entity?.let {
                CollectionMapper().mapFromEntity(it, collection.resourceContainer)
            }
        }

        /** Add collection to the database, return copy of collection that includes database row id. */
        private fun addCollection(collection: Collection, parent: Collection?): Collection {
            val entity = CollectionMapper().mapToEntity(collection).apply {
                parentFk = parent?.id
                dublinCoreFk = metadata.id
            }
            val insertedId = collectionDao.insert(entity, dsl)
            return collection.copy(id = insertedId)
        }

        private fun importCollection(parent: Collection?, node: OtterTreeNode<CollectionOrContent>): Collection? {
            val collection = (node.value as Collection).let { collection ->
                when (relatedBundleDublinCoreId) {
                    null -> addCollection(collection, parent)
                    else -> fetchCollectionFromDb(collection, relatedBundleDublinCoreId)
                    // TODO: If we don't find a corresponding collection, we continue on, setting collection = null.
                    // TODO: ... Eventually, contents will not be created if there is no parentId. This will happen for
                    // TODO: ... front matter until we have another solution.
                }
            }

            val children = (node as? OtterTree<CollectionOrContent>)?.children
            if (children != null) {
                if (collection != null) {
                    val contents = children.filter { it.value is Content }
                    importContent(collection, contents)
                }
                children
                    .filter { it.value is Collection }
                    .forEach {
                        importCollection(collection, it)
                    }
                if (collection != null) {
                    linkChapterResources(collection)
                    linkVerseResources(collection)
                }
            }

            return collection
        }

        private fun importContent(parent: Collection, nodes: List<OtterTreeNode<CollectionOrContent>>) {
            val entities = nodes
                .mapNotNull { it.value as? Content }
                .map { contentMapper.mapToEntity(it).apply { collectionFk = parent.id } }
            if (entities.isNotEmpty()) contentDao.insertNoReturn(*entities.toTypedArray())
        }

        private fun linkVerseResources(parentCollection: Collection) {
            @Suppress("UNCHECKED_CAST")
            val matchingVerses = contentDao.selectLinkableVerses(
                primaryContentTypes,
                helpContentTypes,
                parentCollection.id,
                dublinCoreIdDslVal
            ) as Select<Record3<Int, Int, Int>>

            resourceLinkDao.insertContentResourceNoReturn(matchingVerses)
        }

        private fun linkChapterResources(parentCollection: Collection) {
            @Suppress("UNCHECKED_CAST")
            val matchingVerses = contentDao.selectLinkableChapters(
                helpContentTypes,
                parentCollection.id,
                dublinCoreIdDslVal
            ) as Select<Record3<Int, Int, Int>>

            resourceLinkDao.insertCollectionResourceNoReturn(matchingVerses)
        }
    }
}
