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
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ResourceContainerRepository(
        private val database: AppDatabase
) : IResourceContainerRepository {
    private val collectionDao = database.getCollectionDao()
    private val contentDao = database.getContentDao()
    private val resourceMetadataDao = database.getResourceMetadataDao()
    private val languageDao = database.getLanguageDao()
    private val resourceLinkDao = database.getResourceLinkDao()

    override fun importResourceContainer(rc: ResourceContainer, rcTree: Tree, languageSlug: String): Single<ImportResult> {
        val dublinCore = rc.manifest.dublinCore
        return Completable.fromAction {
            database.transaction { dsl ->
                val language = LanguageMapper().mapFromEntity(languageDao.fetchBySlug(languageSlug, dsl))
                val metadata = dublinCore.mapToMetadata(rc.dir, language)
                val dublinCoreFk = resourceMetadataDao.insert(ResourceMetadataMapper().mapToEntity(metadata), dsl)

                // TODO: Should this only happen if we are importing a "help" RC? What if the help is imported first,
                // TODO: ... then another related bundle RC is imported?
                val relatedDublinCoreIds: List<Int> =
                        linkRelatedResourceContainers(dublinCoreFk, dublinCore.relation, dublinCore.creator, dsl)

                // TODO: Make enum
                if (rc.type() == "help") {
                    if (relatedDublinCoreIds.isEmpty()) {
                        throw ImportException(ImportResult.UNMATCHED_HELP)
                    }
                    relatedDublinCoreIds.forEach { relatedId ->
                        val ih = ImportHelper(dublinCoreFk, relatedId, dsl)
                        ih.importCollection(null, rcTree)
                    }
                } else {
                    val ih = ImportHelper(dublinCoreFk, null, dsl)
                    ih.importCollection(null, rcTree)
                }
            }
        }
                .toSingleDefault(ImportResult.SUCCESS)
                .onErrorReturn { e -> castOrFindImportException(e)?.result ?: ImportResult.LOAD_RC_ERROR }
                .subscribeOn(Schedulers.io())
    }

    private fun linkRelatedResourceContainers(
            dublinCoreFk: Int,
            relations: List<String>,
            creator: String,
            dsl: DSLContext
    ) : List<Int> {
        val relatedIds = mutableListOf<Int>()
        relations.forEach { relation ->
            val parts = relation.split('/')
            // NOTE: We look for derivedFromFk=null since we are looking for the original resource container
            resourceMetadataDao.fetchLatestVersion(parts[0], parts[1], creator, null, dsl)
                    ?.let { relatedDublinCore ->
                        // TODO: Only add link if it doesn't exist already
                        resourceMetadataDao.addLink(dublinCoreFk, relatedDublinCore.id, dsl)
                        relatedIds.add(relatedDublinCore.id)
                    }
        }
        return relatedIds
    }

    inner class ImportHelper(
            val dublinCoreId: Int,
            val relatedBundleDublinCoreId: Int?,
            val dsl: DSLContext
    ) {
        private fun findCollectionId(collection: Collection, containerId: Int): Int? =
                collectionDao.fetchBySlugAndContainerId(collection.slug, containerId)?.id

        private fun addCollection(collection: Collection, parentId: Int?): Int {
            val entity = CollectionMapper().mapToEntity(collection).apply {
                parentFk = parentId
                dublinCoreFk = dublinCoreId
            }
            return collectionDao.insert(entity, dsl)
        }

        fun importCollection(parentId: Int?, node: TreeNode) {
            (node.value as Collection).let { collection ->
                when (relatedBundleDublinCoreId) {
                    null -> addCollection(collection, parentId)
                    else -> findCollectionId(collection, relatedBundleDublinCoreId)
                }
            }.let { collectionId ->
                // TODO: If we don't find a corresponding collection, we continue on, passing null to collectionId.
                // TODO ... Eventually, contents will not be created if there is no parentId. This will happen for front
                // TODO ... matter until we have another solution.
                (node as? Tree)?.let {
                    for (childNode in it.children) {
                        importNode(collectionId, childNode)
                    }
                }
            }
        }

        private fun importNode(parentId: Int?, node: TreeNode) {
            when (node.value) {
                is Collection -> {
                    importCollection(parentId, node)
                }
                is Content -> {
                    if (parentId != null) {
                        importContent(parentId, node)
                    }
                }
            }
        }

        private fun importContent(parentId: Int, node: TreeNode) {
            val content = node.value
            if (content is Content) {
                val entity = ContentMapper().mapToEntity(content).apply { collectionFk = parentId }
                val contentId = contentDao.insert(entity, dsl)
                if (relatedBundleDublinCoreId != null) {
                    linkResource(parentId, contentId, content.start)
                }
            }
        }

        private fun linkResource(parentId: Int, helpContentId: Int, start: Int) {
            when (start) {
                0 -> linkChapterResource(helpContentId, parentId)
                else -> linkVerseResource(helpContentId, parentId, start)
            }
        }

        private fun linkVerseResource(helpContentId: Int, parentId: Int, start: Int) {
            val extraFieldsForInsertIntoTable = listOf(
                    helpContentId,
                    dublinCoreId
            ).map(DSL::`val`)

            @Suppress("UNCHECKED_CAST")
            val select = contentDao.selectVerseByCollectionIdAndStart(
                    collectionId = parentId,
                    start = start,
                    extraFields = extraFieldsForInsertIntoTable
            ) as Select<Record3<Int, Int, Int>>

            resourceLinkDao.insertContentResource(select, dsl)
        }

        private fun linkChapterResource(helpContentId: Int, collectionFk: Int) {
            val resourceEntity = ResourceLinkEntity(
                    id = 0,
                    resourceContentFk = helpContentId,
                    contentFk = null,
                    collectionFk = collectionFk,
                    dublinCoreFk = dublinCoreId
            )
            resourceLinkDao.insert(resourceEntity, dsl)
        }
    }

    private inner class ImportException(val result: ImportResult): Exception()

    private fun castOrFindImportException(e: Throwable): ImportException? =
            if (e is ImportException) e
            else listOfNotNull(e.cause, *e.suppressed)
                    .mapNotNull(this::castOrFindImportException)
                    .firstOrNull()
}