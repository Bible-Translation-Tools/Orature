package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.viewmodel


import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.content.AccessTakes
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.viewmodel.ProjectEditorViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.viewmodel.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*

class ViewTakesViewModel : ViewModel() {
    private val directoryProvider = Injector.directoryProvider
    private val collectionRepository = Injector.collectionRepo
    private val contentRepository = Injector.contentRepository
    private val takeRepository = Injector.takeRepository
    private val pluginRepository = Injector.pluginRepository

    val contentProperty = find(ProjectEditorViewModel::class).activeContentProperty
    private val projectProperty = find(ProjectHomeViewModel::class).selectedProjectProperty
    private var chapterProperty = find(ProjectEditorViewModel::class).activeChildProperty

    val selectedTakeProperty = SimpleObjectProperty<Take>()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property("View Takes")
    val titleProperty = getProperty(ViewTakesViewModel::title)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(ViewTakesViewModel::showPluginActive)

    val snackBarObservable: PublishSubject<Boolean> = PublishSubject.create()

    private val recordTake = RecordTake(
            collectionRepository,
            contentRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            LaunchPlugin(pluginRepository)
    )

    private val accessTakes = AccessTakes(
            Injector.contentRepository,
            Injector.takeRepository
    )

    init {
        reset()
    }

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginView>().openModal()
    }

    private fun populateTakes(content: Content) {
        accessTakes
                .getByContent(content)
                .observeOnFx()
                .subscribe { retrievedTakes ->
                    alternateTakes.clear()
                    alternateTakes.addAll(retrievedTakes.filter { it != content.selectedTake })
                    selectedTakeProperty.value = content.selectedTake
                }
    }

    fun acceptTake(take: Take) {
        val content = contentProperty.value
        // Move the old selected take back to the alternates (if not null)
        if (selectedTakeProperty.value != null) alternateTakes.add(selectedTakeProperty.value)
        // Do the database action
        accessTakes
                .setSelectedTake(content, take)
                .subscribe()
        // Set the new selected take value
        selectedTakeProperty.value = take
        // Remove the new selected take from the alternates
        alternateTakes.remove(take)
    }

    fun setTakePlayed(take: Take) {
        accessTakes
                .setTakePlayed(take, true)
                .subscribe()
    }

    fun recordContent() {
        projectProperty.value?.let { project ->
            showPluginActive = true
            recordTake
                    .record(project, chapterProperty.value, contentProperty.value)
                    .observeOnFx()
                    .doOnComplete {
                        showPluginActive = false
                        populateTakes(contentProperty.value)
                    }
                    .onErrorResumeNext {
                        Completable.fromAction {
                            showPluginActive = false
                            snackBarObservable.onNext(true)
                        }
                    }
                    .subscribe()
        }
    }

    fun delete(take: Take) {
        if (take == selectedTakeProperty.value) {
            // Delete the selected take
            accessTakes
                    .setSelectedTake(contentProperty.value, null)
                    .concatWith(accessTakes.delete(take))
                    .subscribe()
            selectedTakeProperty.value = null
        } else {
            alternateTakes.remove(take)
            accessTakes
                    .delete(take)
                    .subscribe()
        }
        if (take.path.exists()) take.path.delete()
    }

    fun reset() {
        alternateTakes.clear()
        selectedTakeProperty.value = null
        contentProperty.value?.let { populateTakes(it) }
        title = if (contentProperty.value?.labelKey == "chapter") {
            chapterProperty.value?.titleKey ?: ""
        } else {
            "${FX.messages[contentProperty.value?.labelKey ?: "verse"]} ${contentProperty.value?.start ?: ""}"
        }
    }
}