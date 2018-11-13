package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.content.AccessTakes
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.viewmodel.ProjectEditorViewModel
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*
import tornadofx.FX.Companion.messages

class ViewTakesModel {
    private val directoryProvider = Injector.directoryProvider
    private val collectionRepository = Injector.collectionRepo
    private val chunkRepository = Injector.chunkRepository
    private val takeRepository = Injector.takeRepository
    private val pluginRepository = Injector.pluginRepository

    val chunkProperty = find(ProjectEditorViewModel::class).activeChunkProperty
    val projectProperty = find(ProjectHomeViewModel::class).selectedProjectProperty
    var activeChild = find(ProjectEditorViewModel::class).activeChildProperty

    val selectedTakeProperty = SimpleObjectProperty<Take>()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property("View Takes")
    val titleProperty = getProperty(ViewTakesModel::title)

    // Whether the UI should show the plugin as active
    var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(ViewTakesModel::showPluginActive)

    val recordTake = RecordTake(
            collectionRepository,
            chunkRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            LaunchPlugin(pluginRepository)
    )

    private val accessTakes = AccessTakes(
            Injector.chunkRepository,
            Injector.takeRepository
    )

    init {
        reset()
    }

    private fun populateTakes(chunk: Chunk) {
        accessTakes
                .getByChunk(chunk)
                .observeOnFx()
                .subscribe { retrievedTakes ->
                    alternateTakes.clear()
                    alternateTakes.addAll(retrievedTakes.filter { it != chunk.selectedTake })
                    selectedTakeProperty.value = chunk.selectedTake
                }
    }

    fun acceptTake(take: Take) {
        val chunk = chunkProperty.value
        // Move the old selected take back to the alternates (if not null)
        if (selectedTakeProperty.value != null) alternateTakes.add(selectedTakeProperty.value)
        // Do the database action
        accessTakes
                .setSelectedTake(chunk, take)
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

    fun reset() {
        alternateTakes.clear()
        selectedTakeProperty.value = null
        chunkProperty.value?.let { populateTakes(it) }
        title = "${messages[chunkProperty.value?.labelKey ?: "verse"]} ${chunkProperty.value?.start ?: ""}"
    }


    fun recordChunk() {
        projectProperty.value?.let { project ->
            showPluginActive = true
            recordTake
                    .record(project, activeChild.value, chunkProperty.value)
                    .observeOnFx()
                    .subscribe {
                        showPluginActive = false
                        populateTakes(chunkProperty.value)
                    }
        }
    }

    fun delete(take: Take) {
        if (take == selectedTakeProperty.value) {
            // Delete the selected take
            accessTakes
                    .setSelectedTake(chunkProperty.value, null)
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
}

