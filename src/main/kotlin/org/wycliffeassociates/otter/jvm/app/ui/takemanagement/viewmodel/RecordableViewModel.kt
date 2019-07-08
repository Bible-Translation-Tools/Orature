package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import io.reactivex.disposables.CompositeDisposable
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.Recordable
import tornadofx.*

open class RecordableViewModel {

    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordable by recordableProperty

    private val disposables = CompositeDisposable()

    val selectedTakeProperty = SimpleObjectProperty<Take?>()
    val selectedTake by selectedTakeProperty

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    init {
        recordableProperty.onChange {
            clearDisposables()
            it?.audio?.let { audio ->
                loadTakes(audio)
                subscribeToSelectedTake(audio)
            }
        }
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    fun loadTakes(audio: AssociatedAudio) {
        alternateTakes.clear()
        audio.takes
            .subscribe {
                removeOnDeleted(it)
                if ( it != selectedTake ) {
                    Platform.runLater {
                        alternateTakes.add(it)
                    }
                }
            }.let { disposables.add(it) }
    }

    private fun removeOnDeleted(take: Take) {
        take.deletedTimestamp.subscribe { dateHolder ->
            if (dateHolder.value != null) {
                alternateTakes.remove(take)
                if (take == selectedTake) {
                    selectTake(null)
                }
            }
        }.let { disposables.add(it) }
    }

    private fun subscribeToSelectedTake(audio: AssociatedAudio) {
        audio.selected.subscribe {
            selectedTakeProperty.set(it.value)
        }.let { disposables.add(it) }
    }

    fun selectTake(take: Take?) {
        take?.let {
            alternateTakes.remove(it)

            selectedTake?.let { oldSelectedTake ->
                alternateTakes.add(oldSelectedTake)
            }
        }

        // Set the new selected take value
        recordable?.audio?.selectTake(take) ?: throw IllegalStateException("Recordable is null")
    }

    fun deleteTake(take: Take) {
        take.deletedTimestamp.accept(DateHolder.now())
    }
}