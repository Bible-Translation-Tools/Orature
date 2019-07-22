package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.EditTakeEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.PlayOrPauseEvent
import tornadofx.FX.Companion.messages
import tornadofx.*

open class RecordableViewModel(private val audioPluginViewModel: AudioPluginViewModel) {

    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordable by recordableProperty

    private val disposables = CompositeDisposable()

    val selectedTakeProperty = SimpleObjectProperty<Take?>()

    val contextProperty = SimpleObjectProperty<TakeContext>(TakeContext.RECORD)
    val showPluginActiveProperty = SimpleBooleanProperty(false)
    var showPluginActive by showPluginActiveProperty

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent?> = SimpleObjectProperty()

    init {
        recordableProperty.onChange {
            clearDisposables()
            it?.audio?.let { audio ->
                subscribeSelectedTakePropertyToRelay(audio)
                loadTakes(audio)
            }
        }
    }

    fun recordNewTake() {
        recordable?.let {
            contextProperty.set(TakeContext.RECORD)
            showPluginActive = true
            audioPluginViewModel
                .record(it)
                .observeOnFx()
                .subscribe { result ->
                    showPluginActive = false
                    when (result) {
                        RecordTake.Result.NO_RECORDER -> snackBarObservable.onNext(messages["noRecorder"])
                        RecordTake.Result.SUCCESS, RecordTake.Result.NO_AUDIO -> {}
                        null -> {} // This cannot happen but the compiler complains if null branch does not exist
                    }
                }
        } ?: throw IllegalStateException("Recordable is null")
    }

    fun editTake(editTakeEvent: EditTakeEvent) {
        contextProperty.set(TakeContext.EDIT_TAKES)
        showPluginActive = true
        audioPluginViewModel
            .edit(editTakeEvent.take)
            .observeOnFx()
            .subscribe { result ->
                showPluginActive = false
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    EditTake.Result.SUCCESS -> editTakeEvent.onComplete()
                    null -> {} // This cannot happen but the compiler complains if null branch does not exist
                }
            }
    }

    fun selectTake(take: Take?) {
        // selectedTakeProperty will be updated when the relay emits the item that it accepts
        updateAlternateTakes(selectedTakeProperty.value, take)
        recordable?.audio?.selectTake(take) ?: throw IllegalStateException("Recordable is null")
    }

    fun deleteTake(take: Take) {
        take.deletedTimestamp.accept(DateHolder.now())
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    private fun Take.isNotDeleted() = deletedTimestamp.value?.value == null

    private fun loadTakes(audio: AssociatedAudio) {
        alternateTakes.clear()
        // selectedTakeProperty may not have been updated yet so ask for the current selected take
        val selected = audio.selected.value?.value
        audio.takes
            .filter { it.isNotDeleted() }
            .subscribe {
                if ( it != selected ) {
                    addToAlternateTakes(it)
                }
                removeOnDeleted(it)
            }.let { disposables.add(it) }
    }

    private fun removeOnDeleted(take: Take) {
        take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .subscribe {
                removeFromAlternateTakes(take)
            }.let { disposables.add(it) }
    }

    private fun subscribeSelectedTakePropertyToRelay(audio: AssociatedAudio) {
        audio.selected.subscribe {
            selectedTakeProperty.set(it.value)
        }.let { disposables.add(it) }
    }

    private fun updateAlternateTakes(oldSelectedTake: Take?, newSelectedTake: Take?) {
        oldSelectedTake?.let {
            if (it.isNotDeleted()) {
                addToAlternateTakes(it)
            }
        }
        newSelectedTake?.let {
            removeFromAlternateTakes(it)
        }
    }

    private fun addToAlternateTakes(take: Take) {
        Platform.runLater {
            alternateTakes.add(take)
            alternateTakes.sortBy { it.number }
        }
    }

    private fun removeFromAlternateTakes(take: Take) {
        Platform.runLater {
            alternateTakes.remove(take)
        }
    }
}