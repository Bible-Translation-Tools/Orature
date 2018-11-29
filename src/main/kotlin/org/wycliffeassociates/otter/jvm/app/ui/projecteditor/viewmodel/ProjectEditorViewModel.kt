package org.wycliffeassociates.otter.jvm.app.ui.projecteditor.viewmodel

import com.github.thomasnield.rxkotlinfx.changes
import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Content
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
    private val contentRepository = Injector.contentRepository
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

    // List of content to display on the screen
    // Boolean tracks whether the content has takes associated with it
    val allContent: ObservableList<Pair<SimpleObjectProperty<Content>, SimpleBooleanProperty>>
            = FXCollections.observableArrayList()
    val filteredContent: ObservableList<Pair<SimpleObjectProperty<Content>, SimpleBooleanProperty>>
            = FXCollections.observableArrayList()

    private var activeContent: Content by property()
    val activeContentProperty = getProperty(ProjectEditorViewModel::activeContent)

    // What record/review/edit context are we in?
    private var context: ChapterContext by property(ChapterContext.RECORD)
    val contextProperty = getProperty(ProjectEditorViewModel::context)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    val showPluginActiveProperty = getProperty(ProjectEditorViewModel::showPluginActive)

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ProjectEditorViewModel::loading)

    val chapterModeEnabledProperty = SimpleBooleanProperty(false)

    // Create the use cases we need (the model layer)
    private val accessTakes = AccessTakes(contentRepository, takeRepository)
    private val launchPlugin = LaunchPlugin(pluginRepository)
    private val recordTake = RecordTake(
            collectionRepository,
            contentRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            launchPlugin
    )
    private val editTake = EditTake(takeRepository, launchPlugin)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    init {
        projectProperty.toObservable().subscribe { setTitleAndChapters() }
        Observable.merge(chapterModeEnabledProperty.toObservable(), allContent.changes()).subscribe { _ ->
            filteredContent.setAll(
                    if (chapterModeEnabledProperty.value == true) {
                        allContent.filtered { it.first.value?.labelKey == "chapter" }
                    } else {
                        allContent.filtered { it.first.value?.labelKey != "chapter" }
                    }
            )
        }
    }

    fun refreshActiveContent() {
        // See if takes still exist for this content
        activeContentProperty.value?.let {
            accessTakes
                    .getTakeCount(activeContent)
                    .observeOnFx()
                    .subscribe { count ->
                        if (count == 0) {
                            // No more takes. Update hasTakes property
                            filteredContent.filter { it.first.value == activeContent }.first().second.value = false
                        }
                    }
        }
    }

    private fun setTitleAndChapters() {
        val project = projectProperty.value
        activeChunkProperty.value = null
        activeChildProperty.value = null
        if (project != null) {
            projectTitle = project.titleKey
            children.clear()
            filteredContent.clear()
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
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        contentRepository
                .getByCollection(child)
                .flatMapObservable {
                    Observable.fromIterable(it)
                }
                .flatMapSingle { content ->
                    accessTakes
                            .getTakeCount(content)
                            .map { Pair(content.toProperty(), SimpleBooleanProperty(it > 0)) }
                }
                .toList()
                .observeOnFx()
                .subscribe { retrieved ->
                    retrieved.sortBy { it.first.value.sort }
                    allContent.clear() // Make sure any content that might have been added are removed
                    allContent.addAll(retrieved)
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

    fun doContentContextualAction(content: Content) {
        activeContent = content
        when (context) {
            ChapterContext.RECORD -> recordContent()
            ChapterContext.VIEW_TAKES -> viewContentTakes()
            ChapterContext.EDIT_TAKES -> editContent()
        }
    }

    private fun recordContent() {
        projectProperty.value?.let { project ->
            showPluginActive = true
            recordTake
                    .record(project, activeChild, activeContent)
                    .observeOnFx()
                    .doOnComplete {
                        showPluginActive = false
                        // Update the has takes boolean property
                        val item = filteredContent.filtered {
                            it.first.value == activeContent
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

    private fun viewContentTakes() {
        // Launch the select takes page
        // Might be better to use a custom scope to pass the data to the view takes page
        workspace.dock<ViewTakesView>()
    }

    private fun editContent() {
        activeContent.selectedTake?.let { take ->
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