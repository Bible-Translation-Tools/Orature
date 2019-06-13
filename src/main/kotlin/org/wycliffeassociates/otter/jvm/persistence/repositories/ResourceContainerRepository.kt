package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jooq.DSLContext
import org.jooq.Record3
import org.jooq.Select
import org.jooq.impl.DSL
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper
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
        rcTree: Tree,
        languageSlug: String
    ): Single<ImportResult> {
        val dublinCore = rc.manifest.dublinCore
        return Completable.fromAction {
            database.transaction { dsl ->
                val language = LanguageMapper().mapFromEntity(languageDao.fetchBySlug(languageSlug, dsl))
                val metadata = dublinCore.mapToMetadata(rc.file, language)
                val dublinCoreFk = resourceMetadataDao.insert(ResourceMetadataMapper().mapToEntity(metadata), dsl)

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

    private fun linkRelatedResourceContainers(
        dublinCoreFk: Int,
        relations: List<String>,
        creator: String,
        dsl: DSLContext
    ): List<Int> {
        val relatedIds = mutableListOf<Int>()
        relations.forEach { relation ->
            val parts = relation.split('/')
            // NOTE: We look for derivedFromFk=null since we are looking for the original resource container
            resourceMetadataDao.fetchLatestVersion(parts[0], parts[1], creator, null, dsl)
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
        private val mainContentTypes = listOf(ContentType.TEXT)
        private val helpContentTypes = listOf(ContentType.TITLE, ContentType.BODY)
        private val dublinCoreIdDslVal = DSL.`val`(dublinCoreId)

        fun import(node: TreeNode) {
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

        private fun findCollectionId(collection: Collection, containerId: Int): Int? =
            collectionDao.fetchBySlugAndContainerId(collection.slug, containerId)?.id

        private fun addCollection(collection: Collection, parentId: Int?): Int {
            val entity = CollectionMapper().mapToEntity(collection).apply {
                parentFk = parentId
                dublinCoreFk = dublinCoreId
            }
            return collectionDao.insert(entity, dsl)
        }

        private fun importCollection(parentId: Int?, node: TreeNode): Int? {
            val collectionId = (node.value as Collection).let { collection ->
                when (relatedBundleDublinCoreId) {
                    null -> addCollection(collection, parentId)
                    else -> findCollectionId(collection, relatedBundleDublinCoreId)
                    // TODO: If we don't find a corresponding collection, we continue on, passing null to collectionId.
                    // TODO: ... Eventually, contents will not be created if there is no parentId. This will happen for
                    // TODO: ... front matter until we have another solution.
                }
            }

            val children = (node as? Tree)?.children
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

        private fun importContent(parentId: Int, nodes: List<TreeNode>) {
            val entities = nodes
                .mapNotNull { (it.value as? Content) }
                .map { contentMapper.mapToEntity(it).apply { collectionFk = parentId } }
            contentDao.insertNoReturn(*entities.toTypedArray())
        }

        private fun linkVerseResources(parentCollectionId: Int) {
            @Suppress("UNCHECKED_CAST")
            val matchingVerses = contentDao.selectLinkableVerses(
                mainContentTypes,
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