package org.wycliffeassociates.otter.jvm.app.ui.projecteditor.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.content.AccessTakes
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.ChapterContext
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.viewmodel.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesView
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*

class ProjectEditorViewModel: ViewModel() {
    private val directoryProvider = Injector.directoryProvider
    private val collectionRepository = Injector.collectionRepo
    private val chunkRepository = Injector.chunkRepository
    private val takeRepository = Injector.takeRepository
    private val pluginRepository = Injector.pluginRepository

    // Inject the selected project from the project home view model
    private val projectProperty = tornadofx.find<ProjectHomeViewModel>().selectedProjectProperty

    // setup model with fx properties
    private var projectTitle: String by property()
    val projectTitleProperty = getProperty(ProjectEditorViewModel::projectTitle)

    // List of collection children (i.e. the chapters) to display in the list
    var children: ObservableList<Collection> = FXCollections.observableList(mutableListOf())

    // Selected child
    private var activeChild: Collection by property()
    val activeChildProperty = getProperty(ProjectEditorViewModel::activeChild)

    // List of chunks to display on the screen
    // Boolean tracks whether the chunk has takes associated with it
    var chunks: ObservableList<Pair<SimpleObjectProperty<Chunk>, SimpleBooleanProperty>>
            = FXCollections.observableArrayList()

    private var activeChunk: Chunk by property()
    val activeChunkProperty = getProperty(ProjectEditorViewModel::activeChunk)

    // What record/review/edit context are we in?
    private var context: ChapterContext by property(ChapterContext.RECORD)
    val contextProperty = getProperty(ProjectEditorViewModel::context)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    val showPluginActiveProperty = getProperty(ProjectEditorViewModel::showPluginActive)

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ProjectEditorViewModel::loading)

    // Create the use cases we need (the model layer)
    private val accessTakes = AccessTakes(chunkRepository, takeRepository)
    private val launchPlugin = LaunchPlugin(pluginRepository)
    private val recordTake = RecordTake(
            collectionRepository,
            chunkRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            launchPlugin
    )
    private val editTake = EditTake(takeRepository, launchPlugin)

    val snackBarObservable = PublishSubject.create<String>()

    init {
        projectProperty.toObservable().subscribe { setTitleAndChapters() }
    }

    fun refreshActiveChunk() {
        // See if takes still exist for this chunk
        activeChunkProperty.value?.let {
            accessTakes
                    .getTakeCount(activeChunk)
                    .observeOnFx()
                    .subscribe { count ->
                        if (count == 0) {
                            // No more takes. Update hasTakes property
                            chunks.filter { it.first.value == activeChunk }.first().second.value = false
                        }
                    }
        }
    }

    private fun setTitleAndChapters() {
        val project = projectProperty.value
        if (project != null) {
            projectTitle = project.titleKey
            children.clear()
            chunks.clear()
                collectionRepository
                        .getChildren(project)
                        .observeOnFx()
                        .subscribe { childCollections ->
                            // Now we have the children of the project collection
                            children.addAll(childCollections.sortedBy { it.sort })
                        }
            }
    }

    fun changeContext(newContext: ChapterContext) {
        context = newContext
    }

    fun selectChildCollection(child: Collection) {
        activeChild = child
        // Remove existing chunks so the user knows they are outdated
        chunks.clear()
        loading = true
        chunkRepository
                .getByCollection(child)
                .flatMapObservable {
                    Observable.fromIterable(it)
                }
                .flatMapSingle { chunk ->
                    accessTakes
                            .getTakeCount(chunk)
                            .map { Pair(chunk.toProperty(), SimpleBooleanProperty(it > 0)) }
                }
                .toList()
                .observeOnFx()
                .subscribe { retrieved ->
                    retrieved.sortBy { it.first.value.sort }
                    chunks.clear() // Make sure any chunks that might have been added are removed
                    chunks.addAll(retrieved)
                    loading = false
                }
    }

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginView>().openModal()
    }

    fun doChunkContextualAction(chunk: Chunk) {
        activeChunk = chunk
        when (context) {
            ChapterContext.RECORD -> recordChunk()
            ChapterContext.VIEW_TAKES -> viewChunkTakes()
            ChapterContext.EDIT_TAKES -> editChunk()
        }
    }

    private fun recordChunk() {
        projectProperty.value?.let { project ->
            showPluginActive = true
            recordTake
                    .record(project, activeChild, activeChunk)
                    .observeOnFx()
                    .doOnComplete {
                        showPluginActive = false
                        // Update the has takes boolean property
                        val item = chunks.filtered {
                            it.first.value == activeChunk
                        }.first()
                        item.second.value = true
                    }
                    .onErrorResumeNext { Completable.fromAction {
                        showPluginActive = false
                        snackBarObservable.onNext(messages["noRecorder"])
                    } }
                    .subscribe()
        }
    }

    private fun viewChunkTakes() {
        // Launch the select takes page
        // Might be better to use a custom scope to pass the data to the view takes page
        workspace.dock<ViewTakesView>()
    }

    private fun editChunk() {
        activeChunk.selectedTake?.let { take ->
            showPluginActive = true
            editTake
                    .edit(take)
                    .doOnError {
                        snackBarObservable.onNext(messages["noEditor"])
                    }
                    .onErrorComplete()
                    .observeOnFx()
                    .subscribe {
                        showPluginActive = false
                    }
        }
    }
}