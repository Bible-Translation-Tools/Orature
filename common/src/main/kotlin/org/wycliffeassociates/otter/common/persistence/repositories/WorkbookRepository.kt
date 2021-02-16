package org.wycliffeassociates.otter.common.persistence.repositories

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.workbook.*
import java.util.*
import java.util.Collections.synchronizedMap
import javax.inject.Inject

private typealias ModelTake = org.wycliffeassociates.otter.common.data.model.Take
private typealias WorkbookTake = org.wycliffeassociates.otter.common.data.workbook.Take

class WorkbookRepository(private val db: IDatabaseAccessors) : IWorkbookRepository {
    private val logger = LoggerFactory.getLogger(WorkbookRepository::class.java)

    @Inject
    constructor(
        collectionRepository: ICollectionRepository,
        contentRepository: IContentRepository,
        resourceRepository: IResourceRepository,
        resourceMetadataRepo: IResourceMetadataRepository,
        takeRepository: ITakeRepository
    ) : this(
        DefaultDatabaseAccessors(
            collectionRepository,
            contentRepository,
            resourceRepository,
            resourceMetadataRepo,
            takeRepository
        )
    )

    /** Disposers for Relays in the current workbook. */
    private val connections = mutableMapOf<Workbook, CompositeDisposable>()

    override fun get(source: Collection, target: Collection): Workbook {
        // Clear database connections and dispose observables for the
        // previous Workbook if a new one was requested.
        val disposables = mutableListOf<Disposable>()
        val workbook = Workbook(book(source, disposables), book(target, disposables))
        connections[workbook] = CompositeDisposable(disposables)
        return workbook
    }

    override fun closeWorkbook(workbook: Workbook) {
        connections[workbook]?.let {
            it.dispose()
            connections.remove(workbook)
        }
    }

    override fun getSoftDeletedTakes(book: Book): Single<List<ModelTake>> {
        return db.getSoftDeletedTakes(book.resourceMetadata, book.slug)
    }

    override fun getProjects(): Single<List<Workbook>> {
        return db.getDerivedProjects()
            .map { projects ->
                projects.filter { it.resourceContainer?.type == ContainerType.Book }
            }
            .flattenAsObservable { it }
            .flatMapMaybe(::getWorkbook)
            .toList()
    }

    private fun getWorkbook(project: Collection): Maybe<Workbook> {
        return db.getSourceProject(project)
            .map { sourceProject ->
                get(sourceProject, project)
            }
    }

    private fun book(bookCollection: Collection, disposables: MutableList<Disposable>): Book {
        val resourceMetadata = bookCollection.resourceContainer
            ?: throw IllegalStateException("Book collection with id=${bookCollection.id} has null resource container")
        return Book(
            collectionId = bookCollection.id,
            title = bookCollection.titleKey,
            label = bookCollection.labelKey,
            sort = bookCollection.sort,
            slug = bookCollection.slug,
            chapters = constructBookChapters(bookCollection, disposables),
            resourceMetadata = resourceMetadata,
            linkedResources = db.getLinkedResourceMetadata(resourceMetadata),
            subtreeResources = db.getSubtreeResourceMetadata(bookCollection)
        )
    }

    private fun constructBookChapters(
        bookCollection: Collection,
        disposables: MutableList<Disposable>
    ): Observable<Chapter> {
        return Observable.defer {
            db.getChildren(bookCollection)
                .flattenAsObservable { it }
                .concatMapEager { constructChapter(it, disposables).toObservable() }
        }.cache()
    }

    private fun constructChapter(
        chapterCollection: Collection,
        disposables: MutableList<Disposable>
    ): Single<Chapter> {
        return db.getCollectionMetaContent(chapterCollection)
            .map { metaContent ->
                Chapter(
                    title = chapterCollection.titleKey,
                    label = chapterCollection.labelKey,
                    sort = chapterCollection.sort,
                    resources = constructResourceGroups(chapterCollection, disposables),
                    audio = constructAssociatedAudio(metaContent, disposables),
                    chunks = constructChunks(chapterCollection, disposables),
                    subtreeResources = db.getSubtreeResourceMetadata(chapterCollection)
                )
            }
    }

    private fun constructChunks(
        chapterCollection: Collection,
        disposables: MutableList<Disposable>
    ): Observable<Chunk> {
        return Observable.defer {
            db.getContentByCollection(chapterCollection)
                .flattenAsObservable { it }
                .filter { it.type == ContentType.TEXT }
                .map { chunk(it, disposables) }
        }.cache()
    }

    private fun chunk(content: Content, disposables: MutableList<Disposable>): Chunk {
        return Chunk(
            sort = content.sort,
            label = content.labelKey,
            audio = constructAssociatedAudio(content, disposables),
            resources = constructResourceGroups(content, disposables),
            textItem = textItem(content),
            start = content.start,
            end = content.end,
            contentType = content.type
        )
    }

    private fun textItem(content: Content): TextItem {
        return content.format?.let { format ->
            content.text?.let { text ->
                TextItem(text, MimeType.of(format))
            } ?: throw IllegalStateException("Content text is null")
        } ?: TextItem("[empty]", MimeType.USFM)
    }

    private fun constructResource(
        title: Content,
        body: Content?,
        identifier: String,
        disposables: MutableList<Disposable>
    ): Resource? {

        val bodyComponent = body?.let {
            Resource.Component(
                sort = it.sort,
                textItem = textItem(it),
                audio = constructAssociatedAudio(it, disposables),
                contentType = ContentType.BODY,
                label = resourceLabel(ContentType.BODY, identifier)
            )
        }

        val titleComponent = Resource.Component(
            sort = title.sort,
            textItem = textItem(title),
            audio = constructAssociatedAudio(title, disposables),
            contentType = ContentType.TITLE,
            label = resourceLabel(ContentType.TITLE, identifier)
        )

        return Resource(
            title = titleComponent,
            body = bodyComponent
        )
    }

    private fun constructResourceGroups(
        content: Content,
        disposables: MutableList<Disposable>
    ) = constructResourceGroups(
        resourceMetadataList = db.getResourceMetadata(content),
        getResourceContents = { db.getResources(content, it) },
        disposables = disposables
    )

    private fun constructResourceGroups(
        collection: Collection,
        disposables: MutableList<Disposable>
    ) = constructResourceGroups(
        resourceMetadataList = db.getResourceMetadata(collection),
        getResourceContents = { db.getResources(collection, it) },
        disposables = disposables
    )

    private fun constructResourceGroups(
        resourceMetadataList: List<ResourceMetadata>,
        getResourceContents: (ResourceMetadata) -> Observable<Content>,
        disposables: MutableList<Disposable>
    ): List<ResourceGroup> {
        return resourceMetadataList.map {
            val resources = Observable.defer {
                getResourceContents(it)
                    .contentsToResources(it.identifier, disposables)
            }.cache()

            ResourceGroup(it, resources)
        }
    }

    private fun Observable<Content>.contentsToResources(
        identifier: String,
        disposables: MutableList<Disposable>
    ): Observable<Resource> {
        return this
            .buffer(2, 1) // create a rolling window of size 2
            .concatMapIterable { list ->
                val a = list.getOrNull(0)
                val b = list.getOrNull(1)
                listOfNotNull(
                    when {
                        // If the first element isn't a title, skip this pair, because the body
                        // was already used by the previous window.
                        a?.type != ContentType.TITLE -> null

                        // If the second element isn't a body, just use the title. (The second
                        // element will appear again in the next window.)
                        b?.type != ContentType.BODY -> constructResource(a, null, identifier, disposables)

                        // Else, we have a title/body pair, so use it.
                        else -> constructResource(a, b, identifier, disposables)
                    }
                )
            }
    }

    private fun deselectUponDelete(
        take: WorkbookTake,
        selectedTakeRelay: BehaviorRelay<TakeHolder>
    ): Disposable {
        return take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .filter { take == selectedTakeRelay.value?.value }
            .map { TakeHolder(null) }
            .doOnError { e ->
                logger.error("Error in deselectUponDelete, take: $take", e)
            }
            .subscribe(selectedTakeRelay)
    }

    private fun deleteFromDbUponDelete(take: WorkbookTake, modelTake: ModelTake): Disposable {
        return take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .doOnError { e ->
                logger.error("Error in deleteFromDbUponDelete, wb take: $take, model take: $modelTake", e)
            }
            .subscribe {
                db.deleteTake(modelTake, it)
                    .doOnError { e -> logger.error("Error in deleteTake: wb take: $take, model take: $modelTake", e) }
                    .subscribe()
            }
    }

    private fun workbookTake(modelTake: ModelTake): WorkbookTake {
        return WorkbookTake(
            name = modelTake.filename,
            file = modelTake.path,
            number = modelTake.number,
            format = MimeType.WAV, // TODO
            createdTimestamp = modelTake.created,
            deletedTimestamp = BehaviorRelay.createDefault(DateHolder(modelTake.deleted))
        )
    }

    private fun modelTake(workbookTake: WorkbookTake, markers: List<Marker> = listOf()): ModelTake {
        return ModelTake(
            filename = workbookTake.file.name,
            path = workbookTake.file,
            number = workbookTake.number,
            created = workbookTake.createdTimestamp,
            deleted = null,
            played = false,
            markers = markers
        )
    }

    private fun constructAssociatedAudio(
        content: Content,
        disposables: MutableList<Disposable>
    ): AssociatedAudio {
        /** Map to recover model.Take objects from workbook.Take objects. */
        val takeMap = synchronizedMap(WeakHashMap<WorkbookTake, ModelTake>())

        /** The initial selected take, from the DB. */
        val initialSelectedTake = content.selectedTake?.let { workbookTake(it) }

        /** Relay to send selected-take updates out to consumers, but also receive updates from UI. */
        val selectedTakeRelay = BehaviorRelay.createDefault(TakeHolder(initialSelectedTake))

        // When we receive an update, write it to the DB.
        val selectedTakeRelaySubscription = selectedTakeRelay
            .distinctUntilChanged() // Don't write unless changed
            .skip(1) // Don't write the value we just loaded from the DB
            .doOnError { e ->
                logger.error("Error in selectedTakesRelay, content: $content", e)
            }
            .subscribe {
                content.selectedTake = it.value?.let { wbTake -> takeMap[wbTake] }
                db.updateContent(content)
                    .doOnError { e -> logger.error("Error in updating content for content: $content", e) }
                    .subscribe()
            }

        /** Initial Takes read from the DB. */
        val takesFromDb = db.getTakeByContent(content)
            .flattenAsObservable { list: List<ModelTake> -> list.sortedBy { it.number } }
            .map { modelTake ->
                val wbTake = when (modelTake) {
                    content.selectedTake -> initialSelectedTake
                    else -> workbookTake(modelTake)
                }
                wbTake to modelTake
            }

        /** Relay to send Takes out to consumers, but also receive new Takes from UI. */
        val takesRelay = ReplayRelay.create<WorkbookTake>()
        takesFromDb
            // Record the mapping between data types.
            .doOnNext { (wbTake, modelTake) -> takeMap[wbTake] = modelTake }
            // Feed the initial list to takesRelay
            .map { (wbTake, _) -> wbTake }
            .doOnError { e -> logger.error("Error in takesRelay, content: $content", e) }
            .subscribe(takesRelay)

        val takesRelaySubscription = takesRelay
            .filter { it.deletedTimestamp.value?.value == null }
            .map { it to (takeMap[it] ?: modelTake(it)) }
            .doOnNext { (wbTake, modelTake) ->
                // When the selected take becomes deleted, deselect it.
                deselectUponDelete(wbTake, selectedTakeRelay)
                // When a take becomes deleted, delete it from the database
                deleteFromDbUponDelete(wbTake, modelTake)
            }

            // These are new takes
            .filter { (wbTake, _) -> !takeMap.contains(wbTake) }

            // Keep the takeMap current
            .doOnNext { (wbTake, modelTake) ->
                takeMap[wbTake] = modelTake
            }
            .doOnError { e ->
                logger.error("Error constructing associated audio for content: $content", e)
            }

            // Insert the new take into the DB. (We already filtered existing takes out.)
            .subscribe { (_, modelTake) ->
                db.insertTakeForContent(modelTake, content)
                    .doOnError { e -> logger.error("Error inserting take: $modelTake for content: $content", e) }
                    .subscribe { insertionId -> modelTake.id = insertionId }
            }

        synchronized(disposables) {
            disposables.add(takesRelaySubscription)
            disposables.add(selectedTakeRelaySubscription)
        }

        return AssociatedAudio(takesRelay, selectedTakeRelay)
    }

    private fun resourceLabel(contentType: ContentType, identifier: String): String {
        return when (identifier) {
            "tn" -> {
                when (contentType) {
                    ContentType.TITLE -> "snippet"
                    ContentType.BODY -> "note"
                    else -> ""
                }
            }
            "tq" -> {
                when (contentType) {
                    ContentType.TITLE -> "question"
                    ContentType.BODY -> "answer"
                    else -> ""
                }
            }
            else -> ""
        }
    }

    interface IDatabaseAccessors {
        fun getChildren(collection: Collection): Single<List<Collection>>
        fun getCollectionMetaContent(collection: Collection): Single<Content>
        fun getContentByCollection(collection: Collection): Single<List<Content>>
        fun updateContent(content: Content): Completable
        fun getResources(content: Content, metadata: ResourceMetadata): Observable<Content>
        fun getResources(collection: Collection, metadata: ResourceMetadata): Observable<Content>
        fun getResourceMetadata(content: Content): List<ResourceMetadata>
        fun getResourceMetadata(collection: Collection): List<ResourceMetadata>
        fun getLinkedResourceMetadata(metadata: ResourceMetadata): List<ResourceMetadata>
        fun getSubtreeResourceMetadata(collection: Collection): List<ResourceMetadata>
        fun insertTakeForContent(take: ModelTake, content: Content): Single<Int>
        fun getTakeByContent(content: Content): Single<List<ModelTake>>
        fun deleteTake(take: ModelTake, date: DateHolder): Completable
        fun getSoftDeletedTakes(metadata: ResourceMetadata, projectSlug: String): Single<List<ModelTake>>
        fun getDerivedProjects(): Single<List<Collection>>
        fun getSourceProject(targetProject: Collection): Maybe<Collection>
    }
}

private class DefaultDatabaseAccessors(
    private val collectionRepo: ICollectionRepository,
    private val contentRepo: IContentRepository,
    private val resourceRepo: IResourceRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository,
    private val takeRepo: ITakeRepository
) : WorkbookRepository.IDatabaseAccessors {
    override fun getChildren(collection: Collection) = collectionRepo.getChildren(collection)

    override fun getCollectionMetaContent(collection: Collection) = contentRepo.getCollectionMetaContent(collection)
    override fun getContentByCollection(collection: Collection) = contentRepo.getByCollection(collection)
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
    override fun deleteTake(take: ModelTake, date: DateHolder) = takeRepo.update(take.copy(deleted = date.value))

    override fun getSoftDeletedTakes(metadata: ResourceMetadata, projectSlug: String) =
        takeRepo.getSoftDeletedTakes(collectionRepo.getProjectBySlugAndMetadata(projectSlug, metadata).blockingGet())

    override fun getDerivedProjects(): Single<List<Collection>> = collectionRepo.getDerivedProjects()

    override fun getSourceProject(targetProject: Collection): Maybe<Collection> =
        collectionRepo.getSource(targetProject)
}
