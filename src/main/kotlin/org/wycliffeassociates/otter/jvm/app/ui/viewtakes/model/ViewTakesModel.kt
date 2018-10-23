package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.ProjectPageActions
import org.wycliffeassociates.otter.common.domain.ViewTakesActions
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeModel
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ProjectPage
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.viewmodel.ProjectPageViewModel
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*
import tornadofx.FX.Companion.messages

class ViewTakesModel {
    private val directoryProvider = Injector.directoryProvider
    private val collectionRepository = Injector.collectionRepo
    private val chunkRepository = Injector.chunkRepository
    private val takeRepository = Injector.takeRepository
    private val pluginRepository = Injector.pluginRepository

    val chunkProperty = find(ProjectPageViewModel::class).activeChunkProperty
    val projectProperty = find(ProjectHomeViewModel::class).selectedProjectProperty
    var activeChild = find(ProjectPageViewModel::class).activeChild.value


    val selectedTakeProperty = SimpleObjectProperty<Take>()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property("View Takes")
    val titleProperty = getProperty(ViewTakesModel::title)

    // Whether the UI should show the plugin as active
    var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(ViewTakesModel::showPluginActive)

    val projectPageActions = ProjectPageActions(
            directoryProvider,
            WaveFileCreator(),
            collectionRepository,
            chunkRepository,
            takeRepository,
            pluginRepository
    )

    private val viewTakesActions = ViewTakesActions(
            Injector.chunkRepository,
            Injector.takeRepository
    )

    init {
        reset()
    }

    private fun populateTakes(chunk: Chunk) {
        viewTakesActions
                .getTakes(chunk)
                .observeOnFx()
                .subscribe { retrievedTakes ->
                    alternateTakes.clear()
                    alternateTakes.addAll(retrievedTakes.filter { it != chunk.selectedTake })
                    selectedTakeProperty.value = chunk.selectedTake
                }
    }

    fun acceptTake(take: Take) {
        val chunk = chunkProperty.value
        viewTakesActions
                .updateChunkSelectedTake(chunk, take)
                .subscribe()
        selectedTakeProperty.value = take
    }

    fun setTakePlayed(take: Take) {
        viewTakesActions
                .updateTakePlayed(take, true)
                .subscribe()
    }

    fun reset() {

        alternateTakes.clear()
        selectedTakeProperty.value = null
        chunkProperty.value?.let { populateTakes(it) }
        title = "${messages[chunkProperty.value?.labelKey ?: "verse"]} ${ chunkProperty.value?.start ?: "" }"
    }


     fun recordChunk() {
        projectProperty.value?.let { project ->
            showPluginActive = true
            projectPageActions
                    .createNewTake(chunkProperty.value, project, activeChild)
                    .flatMap { take ->
                        projectPageActions
                                .launchDefaultPluginForTake(take)
                                .toSingle { take }
                    }
                    .flatMap {take ->
                        projectPageActions.insertTake(take, chunkProperty.value)
                    }
                    .observeOnFx()
                    .subscribe { _ ->
                        showPluginActive = false
                        selectChildCollection(activeChild)
                    }
        }
    }
    fun selectChildCollection(child: Collection) {
        activeChild = child
        // Remove existing chunks so the user knows they are outdated
        alternateTakes.clear()
        projectPageActions
                .getChunks(child)
                .flatMapObservable {
                    Observable.fromIterable(it)
                }
                .observeOnFx()
                .subscribe { retrieved ->
                    alternateTakes.clear() // Make sure any take cards that might have been added are removed
                    populateTakes(retrieved)
                }
    }
}

