package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jooq.DSLContext
import org.jooq.Record3
import org.jooq.Select
import org.jooq.impl.DSL
import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.resourceLinkEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ResourceMetadataMapper
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ResourceContainerRepository(
    private val database: AppDatabase,
    private val collectionRepository: ICollectionRepository,
    private val resourceRepository: IResourceRepository
) : IResourceContainerRepository {
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
        return Completable.fromAction {
            database.transaction { dsl ->
                val language = LanguageMapper().mapFromEntity(languageDao.fetchBySlug(languageSlug, dsl))
                val metadata = dublinCore.mapToMetadata(rc.file, language)
                    .let {
                        insertMetadataOrThrow(dsl, it)
                    }

                val relatedDublinCoreIds: List<Int> =
                    linkRelatedResourceContainers(metadata, dublinCore.relation, dublinCore.creator, dsl)

                if (ContainerType.of(rc.type()) == ContainerType.Help) {
                    if (relatedDublinCoreIds.isEmpty()) {
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
            .onErrorReturn { e -> e.castOrFindImportException()?.result ?: ImportResult.LOAD_RC_ERROR }
            .subscribeOn(Schedulers.io())
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
        if (existingRow != null) {
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

    inner class ImportHelper(
        private val metadata: ResourceMetadata,
        private val relatedBundleDublinCoreId: Int?,
        private val dsl: DSLContext
    ) {
        private val dublinCoreIdDslVal = DSL.`val`(metadata.id)

        fun import(node: OtterTreeNode<CollectionOrContent>) {
            importCollection(null, node)

            relatedBundleDublinCoreId
                ?.let(this::findRootCollectionsForRc)
                ?.forEach(resourceRepository::calculateAndSetSubtreeHasResources)
        }

        private fun findRootCollectionsForRc(dublinCoreId: Int): List<Int> {
            return collectionRepository
                .getRootSources()
                .blockingGet()
                .filter { it.resourceContainer?.id == dublinCoreId }
                .map { it.id }
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
            val chapterHelps = contentDao.fetchByCollectionIdAndStart(parentCollection.id, 0, helpContentTypes)

            val resourceEntities = chapterHelps
                .map { helpContent ->
                    resourceLinkEntity(
                        resource = helpContent,
                        collection = CollectionMapper().mapToEntity(parentCollection),
                        metadata = ResourceMetadataMapper().mapToEntity(metadata)
                    )
                }
                .toTypedArray()

            resourceLinkDao.insertNoReturn(*resourceEntities, dsl = dsl)
        }
    }
}