package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.ViewTakesActions
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.viewmodel.ProjectPageViewModel
import tornadofx.*

class ViewTakesModel {

    val chunkProperty = find(ProjectPageViewModel::class).activeChunkProperty

    val selectedTakeProperty = SimpleObjectProperty<Take>()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property("View Takes")
    val titleProperty = getProperty(ViewTakesModel::title)

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
        // TODO: Use localized key
        title = chunkProperty.value?.labelKey ?: "View Takes"
    }
}

