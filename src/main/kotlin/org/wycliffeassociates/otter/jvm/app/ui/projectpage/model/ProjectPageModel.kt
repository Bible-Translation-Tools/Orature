package org.wycliffeassociates.otter.jvm.app.ui.projectpage.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.content.GetContent
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ChapterContext
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesView
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*

class ProjectPageModel {
    val directoryProvider = Injector.directoryProvider
    val collectionRepository = Injector.collectionRepo
    val chunkRepository = Injector.chunkRepository
    val takeRepository = Injector.takeRepository
    val pluginRepository = Injector.pluginRepository

    // Inject the selected project from the project home view model
    val projectProperty = find<ProjectHomeViewModel>().selectedProjectProperty

    // setup model with fx properties
    var projectTitle: String by property()
    val projectTitleProperty = getProperty(ProjectPageModel::projectTitle)

    // List of collection children (i.e. the chapters) to display in the list
    var children: ObservableList<Collection> = FXCollections.observableList(mutableListOf())

    // Selected child
    var activeChild: Collection by property()
    val activeChildProperty = getProperty(ProjectPageModel::activeChild)

    // List of chunks to display on the screen
    // Boolean tracks whether the chunk has takes associated with it
    var chunks: ObservableList<Pair<Chunk, Boolean>> = FXCollections.observableArrayList()

    var activeChunk: Chunk by property()
    var activeChunkProperty = getProperty(ProjectPageModel::activeChunk)

    // What record/review/edit context are we in?
    var context: ChapterContext by property(ChapterContext.RECORD)
    var contextProperty = getProperty(ProjectPageModel::context)

    // Whether the UI should show the plugin as active
    var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(ProjectPageModel::showPluginActive)

    // Keep a view context to start transitions
    var workspace: Workspace? = null

    // Create the use cases we need
    val getContent = GetContent(
            collectionRepository,
            chunkRepository,
            takeRepository
    )
    val launchPlugin = LaunchPlugin(pluginRepository)
    val recordTake = RecordTake(
            collectionRepository,
            chunkRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            launchPlugin
    )
    val editTake = EditTake(
            takeRepository,
            launchPlugin
    )

    init {
        setTitleAndChapters()
        projectProperty.onChange { setTitleAndChapters() }
    }

    private fun setTitleAndChapters() {
        projectTitle = projectProperty.value.titleKey
        children.clear()
        if (projectProperty.value != null) {
            getContent
                    .getSubcollections(projectProperty.value)
                    .observeOnFx()
                    .subscribe { childCollections ->
                        // Now we have the children of the project collection
                        children.addAll(childCollections.sortedBy { it.sort })
                    }
        }
    }

    fun selectChildCollection(child: Collection) {
        activeChild = child
        // Remove existing chunks so the user knows they are outdated
        chunks.clear()
        getContent
                .getChunks(child)
                .flatMapObservable {
                    Observable.fromIterable(it)
                }
                .flatMapSingle { chunk ->
                    getContent
                            .getTakeCount(chunk)
                            .map { Pair(chunk, it > 0) }
                }
                .toList()
                .observeOnFx()
                .subscribe { retrieved ->
                    retrieved.sortBy { it.first.sort }
                    chunks.clear() // Make sure any chunks that might have been added are removed
                    chunks.addAll(retrieved)
                }
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
                    .recordForChunk(project, activeChild, activeChunk)
                    .observeOnFx()
                    .subscribe {
                        showPluginActive = false
                        selectChildCollection(activeChild)
                    }
        }
    }

    private fun viewChunkTakes() {
        // Launch the select takes page
        // Might be better to use a custom scope to pass the data to the view takes page
        workspace?.dock<ViewTakesView>()
    }

    private fun editChunk() {
        activeChunk.selectedTake?.let { take ->
            showPluginActive = true
            editTake
                    .edit(take)
                    .observeOnFx()
                    .subscribe {
                        showPluginActive = false
                    }
        }
    }
}
