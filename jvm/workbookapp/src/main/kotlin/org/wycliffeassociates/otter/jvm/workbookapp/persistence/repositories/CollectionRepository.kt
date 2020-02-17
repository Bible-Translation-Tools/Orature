package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import jooq.Tables.*
import jooq.tables.CollectionEntity.COLLECTION_ENTITY
import jooq.tables.ContentDerivative.CONTENT_DERIVATIVE
import jooq.tables.ContentEntity.CONTENT_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ResourceMetadataMapper
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.*
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate

class CollectionRepository(
    private val database: AppDatabase,
    private val directoryProvider: IDirectoryProvider,
    private val collectionMapper: CollectionMapper = CollectionMapper(),
    private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
    private val languageMapper: LanguageMapper = LanguageMapper(),
    private val dublinCoreCreator: String = OratureInfo.SUITE_NAME
) : ICollectionRepository {

    val log = LoggerFactory.getLogger(CollectionRepository::class.java)

    private val collectionDao = database.collectionDao
    private val metadataDao = database.resourceMetadataDao
    private val languageDao = database.languageDao
    private val contentTypeDao = database.contentTypeDao
    private val resourceMetadataDao = database.resourceMetadataDao

    override fun delete(obj: Collection): Completable {
        return Completable
            .fromAction {
                collectionDao.delete(collectionMapper.mapToEntity(obj))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun deleteProject(project: Collection, deleteAudio: Boolean): Completable {
        return Completable
            .fromAction {
                // 1. Delete the project collection from the database. The associated chunks, takes, and links
                //    should be cascade deleted
                collectionDao.delete(collectionMapper.mapToEntity(project))
                // 2. Load the resource container
                val metadata = project.resourceContainer
                if (metadata != null) {
                    ResourceContainer.load(metadata.path).use { container ->
                        // 3. Remove the project from the manifest
                        container.manifest.projects = container
                            .manifest
                            .projects
                            .filter { it.identifier != project.slug }
                        // 4a. If the manifest has more projects, write out the new manifest
                        if (container.manifest.projects.isNotEmpty()) {
                            container.writeManifest()
                        } else {
                            // 4b. If the manifest has no projects left,
                            // delete the RC folder and the metadata from the database
                            metadata.path.deleteRecursively()
                            val links = metadataDao.fetchLinks(metadata.id)
                            links.forEach {
                                metadataDao.delete(it)
                            }
                            metadataDao.delete(metadataMapper.mapToEntity(metadata))
                        }
                    }
                }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun deleteResources(project: Collection, deleteAudio: Boolean): Completable {
        return Completable.fromAction {
            val files = arrayListOf<File>()
            database.transaction { dsl ->

                // Get resource content entries related to the project (content of tn/tq)
                val resourceContent = dsl.select(RESOURCE_LINK.RESOURCE_CONTENT_FK)
                    .from(RESOURCE_LINK)
                    .where(
                        RESOURCE_LINK.DUBLIN_CORE_FK.`in`(
                            // get resources linked to source dublin core
                            dsl.select(RC_LINK_ENTITY.RC2_FK)
                                .from(RC_LINK_ENTITY)
                                .where(
                                    RC_LINK_ENTITY.RC1_FK.`in`(
                                        // get source dublin core of derived project
                                        dsl.select(DUBLIN_CORE_ENTITY.DERIVEDFROM_FK)
                                            .from(DUBLIN_CORE_ENTITY)
                                            .where(
                                                DUBLIN_CORE_ENTITY.ID.`in`(
                                                    // get dublin core of project
                                                    dsl.select(COLLECTION_ENTITY.DUBLIN_CORE_FK)
                                                        .from(COLLECTION_ENTITY)
                                                        .where(
                                                            COLLECTION_ENTITY.ID.eq(
                                                                project.id
                                                            )
                                                        )
                                                )
                                            )
                                    )
                                )
                        ).and(
                            // Filter Resource Content to just what's in the project being deleted
                            RESOURCE_LINK.RESOURCE_CONTENT_FK.`in`(
                                dsl.select(CONTENT_ENTITY.ID)
                                    .from(CONTENT_ENTITY)
                                    .where(
                                        CONTENT_ENTITY.COLLECTION_FK.`in`(
                                            // Look up the chapter collection the resource content belongs to
                                            dsl.select(COLLECTION_ENTITY.ID)
                                                .from(COLLECTION_ENTITY)
                                                .where(
                                                    COLLECTION_ENTITY.PARENT_FK.`in`(
                                                        // Look up the project the chapter collection belongs to
                                                        dsl.select(COLLECTION_ENTITY.ID)
                                                            .from(COLLECTION_ENTITY)
                                                            .where(
                                                                // We need the source, not the derived,
                                                                // just use the slug. It will result in derived results
                                                                // in addition to source, but resources aren't attached
                                                                // to the derived anyway
                                                                COLLECTION_ENTITY.SLUG.eq(project.slug)
                                                            )
                                                    )
                                                )
                                        )
                                    )
                            )
                        )
                    )

                // get all files associated with the resource content
                // to delete outside of the db transaction
                val paths = dsl.select(TAKE_ENTITY.PATH)
                    .from(TAKE_ENTITY)
                    .where(
                        TAKE_ENTITY.CONTENT_FK.`in`(
                            resourceContent
                        )
                    ).fetch { it.value1() }
                files.addAll(paths.map { File(it) })

                // delete the take entries of resource content from the database
                dsl.deleteFrom(TAKE_ENTITY)
                    .where(TAKE_ENTITY.CONTENT_FK.`in`(resourceContent))
                    .execute()
            }

            // actually delete the resource recordings
            files.forEach {
                try {
                    it.delete()
                } catch (e: FileNotFoundException) {
                    log.error("File not found when deleting resources of project: $project.", e)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Collection>> {
        return Single
            .fromCallable {
                collectionDao
                    .fetchAll()
                    .map(this::buildCollection)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getDerivedProjects(): Single<List<Collection>> {
        return Single
            .fromCallable {
                collectionDao
                    .fetchByLabel("project")
                    .filter { it.sourceFk != null }
                    .map(this::buildCollection)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getSourceProjects(): Single<List<Collection>> {
        return Single
            .fromCallable {
                collectionDao
                    .fetchByLabel("project")
                    .filter { it.sourceFk == null }
                    .map(this::buildCollection)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getRootSources(): Single<List<Collection>> {
        return Single
            .fromCallable {
                collectionDao
                    .fetchAll()
                    .filter { it.parentFk == null && it.sourceFk == null }
                    .map(this::buildCollection)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getSource(project: Collection): Maybe<Collection> {
        return Maybe
            .fromCallable {
                buildCollection(
                    collectionDao.fetchSource(collectionDao.fetchById(project.id))
                )
            }
            .onErrorComplete()
            .subscribeOn(Schedulers.io())
    }

    override fun getChildren(collection: Collection): Single<List<Collection>> {
        return Single
            .fromCallable {
                collectionDao
                    .fetchChildren(collectionMapper.mapToEntity(collection))
                    .map(this::buildCollection)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun updateSource(collection: Collection, newSource: Collection): Completable {
        return Completable
            .fromAction {
                val entity = collectionDao.fetchById(collection.id)
                entity.sourceFk = newSource.id
                collectionDao.update(entity)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun updateParent(collection: Collection, newParent: Collection): Completable {
        return Completable
            .fromAction {
                val entity = collectionDao.fetchById(collection.id)
                entity.parentFk = newParent.id
                collectionDao.update(entity)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insert(collection: Collection): Single<Int> {
        return Single
            .fromCallable {
                collectionDao.insert(collectionMapper.mapToEntity(collection))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Collection): Completable {
        return Completable
            .fromAction {
                val entity = collectionDao.fetchById(obj.id)
                val newEntity = collectionMapper.mapToEntity(obj, entity.parentFk, entity.sourceFk)
                collectionDao.update(newEntity)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun deriveProject(
        sourceMetadatas: List<ResourceMetadata>,
        sourceCollection: Collection,
        language: Language
    ): Single<Collection> {
        return Single
            .fromCallable {
                database.transactionResult { dsl ->

                    val derivedMetadata = deriveAndLinkMetadata(sourceMetadatas, language, dsl)
                    val mainDerivedMetadata = derivedMetadata.first()

                    // Insert the derived project
                    val sourceCollectionEntity = collectionDao.fetchById(sourceCollection.id, dsl)
                    val projectEntity = deriveProjectCollection(sourceCollectionEntity, mainDerivedMetadata, dsl)

                    // Copy the chapters
                    copyChapters(dsl, sourceCollectionEntity.id, projectEntity.id, mainDerivedMetadata.id)

                    // Copy the content
                    copyContent(dsl, sourceCollectionEntity.id, mainDerivedMetadata.id)

                    // Link the derivative content
                    linkDerivativeContent(dsl, sourceCollectionEntity.id, projectEntity.id)

                    val metadataSourceToDerivedMap = sourceMetadatas.zip(derivedMetadata).associate { it }
                    copyResourceLinks(dsl, projectEntity, metadataSourceToDerivedMap)

                    // Add a project to the container if necessary
                    // Load the existing resource container and see if we need to add another project
                    ResourceContainer.load(File(mainDerivedMetadata.path)).use { container ->
                        if (container.manifest.projects.none { it.identifier == sourceCollection.slug }) {
                            container.manifest.projects = container.manifest.projects.plus(
                                project {
                                    sort = if (
                                        mainDerivedMetadata.subject.toLowerCase() == "bible" &&
                                        projectEntity.sort > 39
                                    ) {
                                        projectEntity.sort + 1
                                    } else {
                                        projectEntity.sort
                                    }
                                    identifier = projectEntity.slug
                                    path = "./${projectEntity.slug}"
                                    // This title will not be localized into the target language
                                    title = projectEntity.title
                                    // Unable to get categories and versification from the source collection
                                }
                            )
                            // Update the container
                            container.write()
                        }
                    }
                    return@transactionResult collectionMapper.mapFromEntity(
                        projectEntity,
                        metadataMapper.mapFromEntity(mainDerivedMetadata, language)
                    )
                }
            }
            .subscribeOn(Schedulers.io())
    }

    private fun deriveProjectCollection(
        sourceEntity: CollectionEntity,
        derivedMetadata: ResourceMetadataEntity,
        dsl: DSLContext
    ): CollectionEntity {
        return sourceEntity
            .copy(
                id = 0,
                parentFk = null,
                dublinCoreFk = derivedMetadata.id,
                sourceFk = sourceEntity.id
            )
            .let { derivedProject ->
                val id = collectionDao.insert(derivedProject, dsl)
                derivedProject.copy(id = id)
            }
    }

    private fun deriveAndLinkMetadata(
        sourceMetadatas: List<ResourceMetadata>,
        newLanguage: Language,
        dsl: DSLContext
    ): List<ResourceMetadataEntity> {
        val derivedMetadata = sourceMetadatas.map {
            findOrInsertMetadataEntity(dsl, it, newLanguage)
        }

        val mainDerived = derivedMetadata.first()
        val linkDerived = derivedMetadata.drop(1)
        linkDerived.forEach {
            resourceMetadataDao.addLink(mainDerived.id, it.id, dsl)
        }
        return derivedMetadata
    }

    private fun findOrInsertMetadataEntity(
        dsl: DSLContext,
        source: ResourceMetadata,
        language: Language
    ): ResourceMetadataEntity {
        // Check for existing resource containers
        val existingMetadata = metadataDao.fetchAll(dsl)
        val matches = existingMetadata.filter {
            it.identifier == source.identifier &&
                    it.languageFk == language.id &&
                    it.creator == dublinCoreCreator &&
                    it.version == source.version &&
                    it.derivedFromFk == source.id
        }

        val metadataEntity = if (matches.isEmpty()) {
            // This combination of identifier and language does not already exist; create it
            createResourceContainer(source, language).use { container ->
                // Convert DublinCore to ResourceMetadata
                val metadata = container.manifest.dublinCore
                    .mapToMetadata(container.file, language)

                // Insert ResourceMetadata into database
                val entity = metadataMapper.mapToEntity(metadata)
                entity.derivedFromFk = source.id
                entity.id = metadataDao.insert(entity, dsl)
                entity
            }
        } else {
            // Use the existing metadata
            matches.first()
        }
        return metadataEntity
    }

    private fun createResourceContainer(source: ResourceMetadata, targetLanguage: Language): ResourceContainer {
        val derivedContainerType = when (source.type) {
            ContainerType.Bundle -> ContainerType.Book // Sources can be bundles, but not our derived containers.
            else -> source.type
        }
        val dublinCore = dublincore {
            identifier = source.identifier
            issued = LocalDate.now().toString()
            modified = LocalDate.now().toString()
            language = language {
                identifier = targetLanguage.slug
                direction = targetLanguage.direction
                title = targetLanguage.name
            }
            creator = dublinCoreCreator
            version = source.version
            format = MimeType.USFM.norm
            subject = source.subject
            type = derivedContainerType.slug
            title = source.title
        }
        val directory = directoryProvider.getDerivedContainerDirectory(
            // A placeholder file is needed here for the mapping function
            // The file is never used, since the DP doesn't look at the directory
            // to generate the derived directory.
            dublinCore.mapToMetadata(File("."), targetLanguage),
            source
        )
        val container = ResourceContainer.create(directory) {
            // Set up the manifest
            manifest = Manifest(
                dublinCore,
                listOf(),
                Checking()
            )
        }
        container.write()
        return container
    }

    private fun copyChapters(dsl: DSLContext, sourceId: Int, projectId: Int, metadataId: Int) {
        // Copy all the chapter collections
        dsl.insertInto(
            COLLECTION_ENTITY,
            COLLECTION_ENTITY.PARENT_FK,
            COLLECTION_ENTITY.SOURCE_FK,
            COLLECTION_ENTITY.LABEL,
            COLLECTION_ENTITY.TITLE,
            COLLECTION_ENTITY.SLUG,
            COLLECTION_ENTITY.SORT,
            COLLECTION_ENTITY.DUBLIN_CORE_FK
        ).select(
            dsl.select(
                value(projectId),
                COLLECTION_ENTITY.ID,
                COLLECTION_ENTITY.LABEL,
                COLLECTION_ENTITY.TITLE,
                COLLECTION_ENTITY.SLUG,
                COLLECTION_ENTITY.SORT,
                value(metadataId)
            )
                .from(COLLECTION_ENTITY)
                .where(COLLECTION_ENTITY.PARENT_FK.eq(sourceId))
        ).execute()
    }

    private fun copyContent(dsl: DSLContext, sourceId: Int, metadataId: Int) {
        dsl.insertInto(
            CONTENT_ENTITY,
            CONTENT_ENTITY.COLLECTION_FK,
            CONTENT_ENTITY.LABEL,
            CONTENT_ENTITY.START,
            CONTENT_ENTITY.SORT,
            CONTENT_ENTITY.TYPE_FK
        )
            .select(
                dsl.select(
                    COLLECTION_ENTITY.ID,
                    field("verselabel", String::class.java),
                    field("versestart", Int::class.java),
                    field("versesort", Int::class.java),
                    field("typefk", Int::class.java)
                )
                    .from(
                        dsl.select(
                            CONTENT_ENTITY.ID.`as`("verseid"),
                            CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid"),
                            CONTENT_ENTITY.LABEL.`as`("verselabel"),
                            CONTENT_ENTITY.START.`as`("versestart"),
                            CONTENT_ENTITY.SORT.`as`("versesort"),
                            CONTENT_ENTITY.TYPE_FK.`as`("typefk")
                        )
                            .from(CONTENT_ENTITY)
                            .where(
                                CONTENT_ENTITY.COLLECTION_FK.`in`(
                                    dsl
                                        .select(COLLECTION_ENTITY.ID)
                                        .from(COLLECTION_ENTITY)
                                        .where(COLLECTION_ENTITY.PARENT_FK.eq(sourceId))
                                )
                            )
                    )
                    .leftJoin(COLLECTION_ENTITY)
                    .on(
                        COLLECTION_ENTITY.SOURCE_FK.eq(field("chapterid", Int::class.java))
                            .and(COLLECTION_ENTITY.DUBLIN_CORE_FK.eq(metadataId))
                    )
            ).execute()
    }

    private fun copyResourceLinks(
        dsl: DSLContext,
        project: CollectionEntity,
        metadataSourceToDerived: Map<ResourceMetadata, ResourceMetadataEntity>
    ) {
        val sourceResourceCont = CONTENT_ENTITY.`as`("sourceResourceContent")
        val derivedResourceCont = CONTENT_ENTITY.`as`("derivedResourceContent")
        val sourceColl = COLLECTION_ENTITY.`as`("sourceCollectionTable")
        val derivedColl = COLLECTION_ENTITY.`as`("derivedCollectionTable")

        val derivedContentColumn = CONTENT_DERIVATIVE.CONTENT_FK
        val derivedCollectionColumn = derivedColl.ID
        val derivedResourceColumn = derivedResourceCont.ID

        metadataSourceToDerived.forEach { sourceMetadata, derivedMetadata ->
            dsl
                .insertInto(
                    RESOURCE_LINK,
                    RESOURCE_LINK.RESOURCE_CONTENT_FK,
                    RESOURCE_LINK.DUBLIN_CORE_FK,
                    RESOURCE_LINK.COLLECTION_FK,
                    RESOURCE_LINK.CONTENT_FK
                )
                .select(
                    dsl
                        .select(
                            derivedResourceColumn,
                            `val`(derivedMetadata.id),
                            derivedCollectionColumn,
                            derivedContentColumn
                        )
                        .from(RESOURCE_LINK)
                        // Map RESOURCE_CONTENT_FK to new resource fk, by joining two CONTENT_ENTITY tables on details.
                        .join(sourceResourceCont).on(RESOURCE_LINK.RESOURCE_CONTENT_FK.eq(sourceResourceCont.ID))
                        .join(derivedResourceCont).on(
                            and(
                                derivedResourceCont.TYPE_FK.eq(sourceResourceCont.TYPE_FK),
                                derivedResourceCont.START.eq(sourceResourceCont.START),
                                derivedResourceCont.COLLECTION_FK.eq(project.id)
                            )
                        )
                        // Map CONTENT_FK to new book content using CONTENT_DERIVATIVE table
                        .leftJoin(CONTENT_DERIVATIVE).on(RESOURCE_LINK.CONTENT_FK.eq(CONTENT_DERIVATIVE.SOURCE_FK))
                        // Map COLLECTION_FK to derived collection. Join two COLLECTION_ENTITY tables on details.
                        .leftJoin(sourceColl).on(RESOURCE_LINK.COLLECTION_FK.eq(sourceColl.ID))
                        .leftJoin(derivedColl).on(
                            and(
                                derivedColl.SLUG.eq(sourceColl.SLUG),
                                derivedColl.LABEL.eq(sourceColl.LABEL),
                                derivedColl.DUBLIN_CORE_FK.eq(project.dublinCoreFk ?: -1)
                            )
                        )
                        .where(
                            and(
                                or(derivedContentColumn.isNotNull, derivedCollectionColumn.isNotNull),
                                RESOURCE_LINK.DUBLIN_CORE_FK.eq(sourceMetadata.id)
                            )
                        )
                ).execute()
        }
    }

    private fun linkDerivativeContent(dsl: DSLContext, sourceId: Int, projectId: Int) {
        dsl.insertInto(
            CONTENT_DERIVATIVE,
            CONTENT_DERIVATIVE.CONTENT_FK,
            CONTENT_DERIVATIVE.SOURCE_FK
        ).select(
            dsl.select(
                field("derivedid", Int::class.java),
                field("sourceid", Int::class.java)
            )
                .from(
                    dsl.select(
                        field("sourceid", Int::class.java),
                        field("sourcesort", Int::class.java),
                        field("sourcetype", Int::class.java),
                        COLLECTION_ENTITY.SLUG.`as`("sourcechapter")
                    )
                        .from(
                            dsl.select(
                                CONTENT_ENTITY.ID.`as`("sourceid"),
                                CONTENT_ENTITY.SORT.`as`("sourcesort"),
                                CONTENT_ENTITY.TYPE_FK.`as`("sourcetype"),
                                CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid")
                            ).from(CONTENT_ENTITY).where(
                                CONTENT_ENTITY.COLLECTION_FK.`in`(
                                    dsl
                                        .select(COLLECTION_ENTITY.ID)
                                        .from(COLLECTION_ENTITY)
                                        .where(COLLECTION_ENTITY.PARENT_FK.eq(sourceId))
                                )
                            )
                        )
                        .leftJoin(COLLECTION_ENTITY)
                        .on(COLLECTION_ENTITY.ID.eq(field("chapterid", Int::class.java)))
                )
                .leftJoin(
                    dsl
                        .select(
                            field("derivedid", Int::class.java),
                            field("derivedsort", Int::class.java),
                            field("derivedtype", Int::class.java),
                            COLLECTION_ENTITY.SLUG.`as`("derivedchapter")
                        )
                        .from(
                            dsl
                                .select(
                                    CONTENT_ENTITY.ID.`as`("derivedid"),
                                    CONTENT_ENTITY.SORT.`as`("derivedsort"),
                                    CONTENT_ENTITY.TYPE_FK.`as`("derivedtype"),
                                    CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid")
                                )
                                .from(CONTENT_ENTITY)
                                .where(
                                    CONTENT_ENTITY.COLLECTION_FK.`in`(
                                        dsl
                                            .select(COLLECTION_ENTITY.ID)
                                            .from(COLLECTION_ENTITY)
                                            .where(COLLECTION_ENTITY.PARENT_FK.eq(projectId))
                                    )
                                )
                        )
                        .leftJoin(COLLECTION_ENTITY)
                        .on(COLLECTION_ENTITY.ID.eq(field("chapterid", Int::class.java)))
                )
                .on(
                    field("sourcesort", Int::class.java)
                        .eq(field("derivedsort", Int::class.java))
                        .and(
                            field("sourcechapter", Int::class.java)
                                .eq(field("derivedchapter", Int::class.java))
                        )
                        .and(
                            field("sourcetype", Int::class.java)
                                .eq(field("derivedtype", Int::class.java))
                        )
                )
        ).execute()
    }

    private fun buildCollection(entity: CollectionEntity): Collection {
        var metadata: ResourceMetadata? = null
        entity.dublinCoreFk?.let {
            val metadataEntity = metadataDao.fetchById(it)
            val language = languageMapper.mapFromEntity(languageDao.fetchById(metadataEntity.languageFk))
            metadata = metadataMapper.mapFromEntity(metadataEntity, language)
        }

        return collectionMapper.mapFromEntity(entity, metadata)
    }
}