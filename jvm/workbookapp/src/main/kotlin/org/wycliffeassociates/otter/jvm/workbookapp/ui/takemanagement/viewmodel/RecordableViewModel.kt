package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservableChanges
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.controls.card.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.util.concurrent.Callable

open class RecordableViewModel(
    private val audioPluginViewModel: AudioPluginViewModel
) : ViewModel() {
    val injector: Injector by inject()
    val workbookViewModel: WorkbookViewModel by inject()

    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordable by recordableProperty

    private val disposables = CompositeDisposable()

    val selectedTakeProperty = SimpleObjectProperty<Take?>()
    val currentTakeProperty = SimpleObjectProperty<Int?>()

    val contextProperty = SimpleObjectProperty<TakeContext>(TakeContext.RECORD)
    val showPluginActiveProperty = SimpleBooleanProperty(false)
    var showPluginActive by showPluginActiveProperty

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())
    val takeCardModels: ObservableList<TakeCardModel> = FXCollections.observableArrayList()

    val sourceAudioAvailableProperty = workbookViewModel.sourceAudioAvailableProperty
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    init {
        recordableProperty.onChange {
            clearDisposables()
            it?.audio?.let { audio ->
                subscribeSelectedTakePropertyToRelay(audio)
                loadTakes(audio)
            }
        }

        alternateTakes.onChange {
            takeCardModels.setAll(
                it.list.map { take ->
                    val ap = injector.audioPlayer
                    ap.load(take.file)
                    TakeCardModel(
                        take,
                        ap,
                        messages["edit"].capitalize(),
                        messages["delete"].capitalize(),
                        messages["play"].capitalize(),
                        messages["pause"].capitalize()
                    )
                }
            )
        }

        workbookViewModel.sourceAudioProperty.onChangeAndDoNow {
            it?.let { source ->
                val audioPlayer = injector.audioPlayer
                audioPlayer.loadSection(source.file, source.start, source.end)
                sourceAudioPlayerProperty.set(audioPlayer)
            }
        }

        audioPluginViewModel.pluginNameProperty.bind(pluginNameBinding())
    }

    fun recordNewTake() {
        recordable?.let { rec ->
            contextProperty.set(TakeContext.RECORD)

            rec.audio.getNewTakeNumber()
                .doOnSuccess { take ->
                    currentTakeProperty.set(take)
                }
                .flatMapMaybe {
                    audioPluginViewModel.getRecorder()
                }
                .doOnSuccess { plugin ->
                    showPluginActive = !plugin.isNativePlugin()
                }
                .flatMapSingle {
                    audioPluginViewModel.record(rec)
                }
                .observeOnFx()
                .subscribe { result: RecordTake.Result ->
                    showPluginActive = false
                    when (result) {
                        RecordTake.Result.NO_RECORDER -> snackBarObservable.onNext(messages["noRecorder"])
                        RecordTake.Result.SUCCESS, RecordTake.Result.NO_AUDIO -> {
                        }
                    }
                }
        } ?: throw IllegalStateException("Recordable is null")
    }

    fun editTake(editTakeEvent: EditTakeEvent) {
        contextProperty.set(TakeContext.EDIT_TAKES)
        currentTakeProperty.set(editTakeEvent.take.number)
        audioPluginViewModel
            .getEditor()
            .flatMapSingle { plugin ->
                Single.just(plugin.isNativePlugin())
            }
            .flatMap { isNative ->
                showPluginActive = !isNative
                audioPluginViewModel.edit(editTakeEvent.take)
            }
            .observeOnFx()
            .subscribe { result: EditTake.Result ->
                showPluginActive = false
                currentTakeProperty.set(null)
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    EditTake.Result.SUCCESS -> editTakeEvent.onComplete()
                }
            }
    }

    fun selectTake(take: Take?) {
        // selectedTakeProperty will be updated when the relay emits the item that it accepts
        updateAlternateTakes(selectedTakeProperty.value, take)
        recordable?.audio?.selectTake(take) ?: throw IllegalStateException("Recordable is null")
    }

    fun selectTake(filename: String) {
        val take = alternateTakes.find { it.name == filename }
        selectTake(take)
    }

    fun deleteTake(take: Take) {
        take.deletedTimestamp.accept(DateHolder.now())
    }

    fun dialogTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                String.format(
                    messages["sourceDialogTitle"],
                    currentTakeProperty.get(),
                    audioPluginViewModel.pluginNameProperty.get()
                )
            },
            audioPluginViewModel.pluginNameProperty,
            currentTakeProperty
        )
    }

    fun dialogTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                String.format(
                    messages["sourceDialogMessage"],
                    currentTakeProperty.get(),
                    audioPluginViewModel.pluginNameProperty.get(),
                    audioPluginViewModel.pluginNameProperty.get()
                )
            },
            audioPluginViewModel.pluginNameProperty,
            currentTakeProperty
        )
    }

    fun pluginNameBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                when (contextProperty.get()) {
                    TakeContext.RECORD -> {
                        audioPluginViewModel.selectedRecorderProperty.get().name
                    }
                    TakeContext.EDIT_TAKES -> {
                        audioPluginViewModel.selectedEditorProperty.get().name
                    }
                    null -> throw IllegalStateException("Action is not supported!")
                }
            },
            contextProperty,
            audioPluginViewModel.selectedRecorderProperty,
            audioPluginViewModel.selectedEditorProperty
        )
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
        // selectedTakeProperty may not have been updated yet so ask for the current selected take
        val selected = audio.selected.value?.value

        val loadedAlternateTakes =
            audio.getAllTakes()
                .filter { it.isNotDeleted() }
                .filter { it != selected }
        alternateTakes.setAll(loadedAlternateTakes)

        audio.takes
            .filter { it.isNotDeleted() }
            .subscribe {
                if (it != selected && !alternateTakes.contains(it)) {
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