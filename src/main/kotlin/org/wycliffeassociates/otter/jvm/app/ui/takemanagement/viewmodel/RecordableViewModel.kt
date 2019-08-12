package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
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

    init {
        recordableProperty.onChange {
            // TODO
        }

        selectedTakeProperty.onChange {
            // TODO
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

    fun editTake(take: Take) {
        contextProperty.set(TakeContext.EDIT_TAKES)
        showPluginActive = true
        audioPluginViewModel
            .edit(take)
            .observeOnFx()
            .subscribe { result ->
                showPluginActive = false
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    EditTake.Result.SUCCESS -> {}
                    null -> {} // This cannot happen but the compiler complains if null branch does not exist
                }
            }
    }

    fun selectTake(take: Take?) {
        // TODO
    }

    fun deleteTake(take: Take) {
        // TODO
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    private fun loadTakes(audio: AssociatedAudio) {
        // TODO
    }

    private fun removeOnDeleted(take: Take) {
        // TODO
    }

    private fun subscribeToSelectedTake(audio: AssociatedAudio) {
        // TODO
    }
}