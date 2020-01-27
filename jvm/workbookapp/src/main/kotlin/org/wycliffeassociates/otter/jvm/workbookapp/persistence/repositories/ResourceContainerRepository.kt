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
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceLinkEntity
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
                val dublinCoreFk = insertMetadataOrThrow(dsl, metadata)

                val relatedDublinCoreIds: List<Int> =
                    linkRelatedResourceContainers(dublinCoreFk, dublinCore.relation, dublinCore.creator, dsl)

                // TODO: Make enum
                if (rc.type() == "help") {
                    if (relatedDublinCoreIds.isEmpty()) {
                        throw ImportException(ImportResult.UNMATCHED_HELP)
                    }
                    relatedDublinCoreIds.forEach { relatedId ->
                        val ih = ImportHelper(dublinCoreFk, relatedId, dsl)
                        ih.import(rcTree)
                    }
                } else {
                    val ih = ImportHelper(dublinCoreFk, null, dsl)
                    ih.import(rcTree)
                }
            }
        }
            .toSingleDefault(ImportResult.SUCCESS)
            .onErrorReturn { e -> e.castOrFindImportException()?.result ?: ImportResult.LOAD_RC_ERROR }
            .subscribeOn(Schedulers.io())
    }

    /** Insert ResourceMetadata, returning row ID, or throw [ImportException] if a matching row already exists. */
    private fun insertMetadataOrThrow(
        dsl: DSLContext,
        metadata: ResourceMetadata
    ): Int {
        val existingRow = resourceMetadataDao.fetchLatestVersion(metadata.language.slug, metadata.identifier)
        if (existingRow != null) {
            throw ImportException(ImportResult.ALREADY_EXISTS)
        }
        val entity = ResourceMetadataMapper().mapToEntity(metadata)
        return resourceMetadataDao.insert(entity, dsl)
    }

    private fun linkRelatedResourceContainers(
        dublinCoreFk: Int,
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
                    resourceMetadataDao.addLink(dublinCoreFk, relatedDublinCore.id, dsl)
                    relatedIds.add(relatedDublinCore.id)
                }
        }
        return relatedIds
    }

    inner class ImportHelper(
        private val dublinCoreId: Int,
        private val relatedBundleDublinCoreId: Int?,
        private val dsl: DSLContext
    ) {
        private val dublinCoreIdDslVal = DSL.`val`(dublinCoreId)

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

        private fun findCollectionId(collection: Collection, containerId: Int): Int? {
            val entity = collectionDao.fetch(
                slug = collection.slug,
                label = collection.labelKey,
                containerId = containerId
            )
            return entity?.id
        }

        private fun addCollection(collection: Collection, parentId: Int?): Int {
            val entity = CollectionMapper().mapToEntity(collection).apply {
                parentFk = parentId
                dublinCoreFk = dublinCoreId
            }
            return collectionDao.insert(entity, dsl)
        }

        private fun importCollection(parentId: Int?, node: OtterTreeNode<CollectionOrContent>): Int? {
            val collectionId = (node.value as Collection).let { collection ->
                when (relatedBundleDublinCoreId) {
                    null -> addCollection(collection, parentId)
                    else -> findCollectionId(collection, relatedBundleDublinCoreId)
                    // TODO: If we don't find a corresponding collection, we continue on, passing null to collectionId.
                    // TODO: ... Eventually, contents will not be created if there is no parentId. This will happen for
                    // TODO: ... front matter until we have another solution.
                }
            }

            val children = (node as? OtterTree<CollectionOrContent>)?.children
            if (children != null) {
                if (collectionId != null) {
                    val contents = children.filter { it.value is Content }
                    importContent(collectionId, contents)
                }
                children
                    .filter { it.value is Collection }
                    .forEach {
                        importCollection(collectionId, it)
                    }
                if (collectionId != null) {
                    linkChapterResources(collectionId)
                    linkVerseResources(collectionId)
                }
            }

            return collectionId
        }

        private fun importContent(parentId: Int, nodes: List<OtterTreeNode<CollectionOrContent>>) {
            val entities = nodes
                .mapNotNull { it.value as? Content }
                .map { contentMapper.mapToEntity(it).apply { collectionFk = parentId } }
            if (entities.isNotEmpty()) contentDao.insertNoReturn(*entities.toTypedArray())
        }

        private fun linkVerseResources(parentCollectionId: Int) {
            @Suppress("UNCHECKED_CAST")
            val matchingVerses = contentDao.selectLinkableVerses(
                primaryContentTypes,
                helpContentTypes,
                parentCollectionId,
                dublinCoreIdDslVal
            ) as Select<Record3<Int, Int, Int>>

            resourceLinkDao.insertContentResourceNoReturn(matchingVerses)
        }

        private fun linkChapterResources(parentCollectionId: Int) {
            val chapterHelps = contentDao.fetchByCollectionIdAndStart(parentCollectionId, 0, helpContentTypes)

            val resourceEntities = chapterHelps
                .map { helpContent ->
                    ResourceLinkEntity(
                        id = 0,
                        resourceContentFk = helpContent.id,
                        contentFk = null,
                        collectionFk = parentCollectionId,
                        dublinCoreFk = dublinCoreId
                    )
                }
                .toTypedArray()

            resourceLinkDao.insertNoReturn(*resourceEntities, dsl = dsl)
        }
    }
}