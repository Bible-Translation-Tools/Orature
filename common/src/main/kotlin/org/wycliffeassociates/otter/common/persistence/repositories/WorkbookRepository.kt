package org.wycliffeassociates.otter.common.persistence.repositories

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.workbook.*
import java.util.*
import java.util.Collections.synchronizedMap

private typealias ModelTake = org.wycliffeassociates.otter.common.data.model.Take
private typealias WorkbookTake = org.wycliffeassociates.otter.common.data.workbook.Take

class WorkbookRepository(private val db: IDatabaseAccessors) : IWorkbookRepository {
    constructor(
        collectionRepository: ICollectionRepository,
        contentRepository: IContentRepository,
        resourceRepository: IResourceRepository,
        takeRepository: ITakeRepository
    ) : this(
        DefaultDatabaseAccessors(
            collectionRepository,
            contentRepository,
            resourceRepository,
            takeRepository
        )
    )

    /** Disposers for Relays in the current workbook. */
    private val connections = CompositeDisposable()

    override fun get(source: Collection, target: Collection): Workbook {
        // Clear database connections and dispose observables for the
        // previous Workbook if a new one was requested.
        connections.clear()
        return Workbook(
            book(source),
            book(target)
        )
    }

    private fun Collection.getResourceMetaData() = this.resourceContainer
        ?: throw IllegalStateException("Collection with id=$id has null resource container")

    private fun book(bookCollection: Collection): Book {
        return Book(
            title = bookCollection.titleKey,
            sort = bookCollection.sort,
            slug = bookCollection.slug,
            chapters = constructBookChapters(bookCollection),
            resourceMetadata = bookCollection.getResourceMetaData(),
            subtreeResources = db.getSubtreeResourceMetadata(bookCollection)
        )
    }

    private fun constructBookChapters(bookCollection: Collection): Observable<Chapter> {
        return Observable.defer {
            db.getChildren(bookCollection)
                .flattenAsObservable { it }
                .concatMapEager { constructChapter(it).toObservable() }
        }.cache()
    }

    private fun constructChapter(chapterCollection: Collection): Single<Chapter> {
        return db.getCollectionMetaContent(chapterCollection)
            .map { metaContent ->
                Chapter(
                    title = chapterCollection.titleKey,
                    sort = chapterCollection.sort,
                    resources = constructResourceGroups(chapterCollection),
                    audio = constructAssociatedAudio(metaContent),
                    chunks = constructChunks(chapterCollection),
                    subtreeResources = db.getSubtreeResourceMetadata(chapterCollection)
                )
            }
    }

    private fun constructChunks(chapterCollection: Collection): Observable<Chunk> {
        return Observable.defer {
            db.getContentByCollection(chapterCollection)
                .flattenAsObservable { it }
                .map(this::chunk)
                .filter {
                    it.contentType == ContentType.TEXT
                }
        }.cache()
    }

    private fun chunk(content: Content) = Chunk(
        sort = content.sort,
        audio = constructAssociatedAudio(content),
        resources = constructResourceGroups(content),
        textItem = textItem(content),
        start = content.start,
        end = content.end,
        contentType = content.type
    )

    private fun textItem(content: Content): TextItem {
        return content.format?.let { format ->
            // TODO 6/25: Content text should never be null, but parse usfm is currently broken so
            // TODO... only check resource contents for now
            if (listOf(ContentType.TITLE, ContentType.BODY).contains(content.type) && content.text == null) {
                throw IllegalStateException("Content text is null for resource")
            }
            TextItem(content.text ?: "[empty]", MimeType.of(format))
        } ?: TextItem(content.text ?: "[empty]", MimeType.of("usfm")) // TODO 7/5: temporary workaround
//        } ?: throw IllegalStateException("Content format is null")
    }

    private fun constructResource(title: Content, body: Content?): Resource? {
        val bodyComponent = body?.let {
            Resource.Component(
                sort = it.sort,
                textItem = textItem(it),
                audio = constructAssociatedAudio(it),
                contentType = ContentType.BODY
            )
        }

        val titleComponent = Resource.Component(
            sort = title.sort,
            textItem = textItem(title),
            audio = constructAssociatedAudio(title),
            contentType = ContentType.TITLE
        )

        return Resource(
            title = titleComponent,
            body = bodyComponent
        )
    }

    private fun constructResourceGroups(content: Content) = constructResourceGroups(
        resourceMetadataList = db.getResourceMetadata(content),
        getResourceContents = { db.getResources(content, it) }
    )

    private fun constructResourceGroups(collection: Collection) = constructResourceGroups(
        resourceMetadataList = db.getResourceMetadata(collection),
        getResourceContents = { db.getResources(collection, it) }
    )

    private fun constructResourceGroups(
        resourceMetadataList: List<ResourceMetadata>,
        getResourceContents: (ResourceMetadata) -> Observable<Content>
    ): List<ResourceGroup> {
        return resourceMetadataList.map {
            val resources = Observable.defer {
                getResourceContents(it)
                    .contentsToResources()
            }.cache()

            ResourceGroup(it, resources)
        }
    }

    private fun Observable<Content>.contentsToResources(): Observable<Resource> {
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
                        b?.type != ContentType.BODY -> constructResource(a, null)

                        // Else, we have a title/body pair, so use it.
                        else -> constructResource(a, b)
                    }
                )
            }
    }

    private fun deselectUponDelete(take: WorkbookTake, selectedTakeRelay: BehaviorRelay<TakeHolder>) {
        val subscription = take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .filter { take == selectedTakeRelay.value?.value }
            .map { TakeHolder(null) }
            .subscribe(selectedTakeRelay)
        connections += subscription
    }

    private fun deleteFromDbUponDelete(take: WorkbookTake, modelTake: ModelTake) {
        val subscription = take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .subscribe {
                db.deleteTake(modelTake, it)
                    .subscribe()
            }
        connections += subscription
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

    private fun constructAssociatedAudio(content: Content): AssociatedAudio {
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
            .subscribe {
                content.selectedTake = it.value?.let { wbTake -> takeMap[wbTake] }
                db.updateContent(content)
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

            // Insert the new take into the DB. (We already filtered existing takes out.)
            .subscribe { (_, modelTake) ->
                db.insertTakeForContent(modelTake, content)
                    .subscribe { insertionId -> modelTake.id = insertionId }
            }

        connections += takesRelaySubscription
        connections += selectedTakeRelaySubscription
        return AssociatedAudio(takesRelay, selectedTakeRelay)
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
        fun getSubtreeResourceMetadata(collection: Collection): List<ResourceMetadata>
        fun insertTakeForContent(take: ModelTake, content: Content): Single<Int>
        fun getTakeByContent(content: Content): Single<List<ModelTake>>
        fun deleteTake(take: ModelTake, date: DateHolder): Completable
    }
}

private class DefaultDatabaseAccessors(
    private val collectionRepo: ICollectionRepository,
    private val contentRepo: IContentRepository,
    private val resourceRepo: IResourceRepository,
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
    override fun getSubtreeResourceMetadata(collection: Collection) =
        resourceRepo.getSubtreeResourceMetadata(collection)

    override fun insertTakeForContent(take: ModelTake, content: Content) = takeRepo.insertForContent(take, content)
    override fun getTakeByContent(content: Content) = takeRepo.getByContent(content, includeDeleted = true)
    override fun deleteTake(take: ModelTake, date: DateHolder) = takeRepo.update(take.copy(deleted = date.value))
}
