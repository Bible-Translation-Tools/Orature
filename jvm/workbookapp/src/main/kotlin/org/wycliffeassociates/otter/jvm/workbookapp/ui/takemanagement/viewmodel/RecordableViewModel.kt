package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.content.MarkTake
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.controls.card.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.MarkerTakeEvent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.util.concurrent.Callable

open class RecordableViewModel(
    private val audioPluginViewModel: AudioPluginViewModel
) : ViewModel() {

    private val logger = LoggerFactory.getLogger(RecordableViewModel::class.java)
    private val injector: Injector by inject()

    val workbookViewModel: WorkbookViewModel by inject()

    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordable by recordableProperty

    val currentTakeNumberProperty = SimpleObjectProperty<Int?>()

    val contextProperty = SimpleObjectProperty<TakeContext>(TakeContext.RECORD)
    val showPluginActiveProperty = SimpleBooleanProperty(false)
    var showPluginActive by showPluginActiveProperty

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    val takeCardModels: ObservableList<TakeCardModel> = FXCollections.observableArrayList()
    val selectedTakeProperty = SimpleObjectProperty<TakeCardModel?>()

    val sourceAudioAvailableProperty = workbookViewModel.sourceAudioAvailableProperty
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    private val disposables = CompositeDisposable()

    init {
        selectedTakeProperty.onChangeAndDoNow {
            sortTakes()
        }

        recordableProperty.onChange {
            clearDisposables()
            it?.audio?.let { audio ->
                subscribeSelectedTakePropertyToRelay(audio)
                loadTakes(audio)
            }
        }

        workbookViewModel.sourceAudioProperty.onChangeAndDoNow { source ->
            var audioPlayer: IAudioPlayer? = null
            if (source != null) {
                audioPlayer = injector.audioPlayer
                audioPlayer.loadSection(source.file, source.start, source.end)
            }
            sourceAudioPlayerProperty.set(audioPlayer)
        }

        audioPluginViewModel.pluginNameProperty.bind(pluginNameBinding())
    }

    fun recordNewTake() {
        recordable?.let { rec ->
            contextProperty.set(TakeContext.RECORD)
            rec.audio.getNewTakeNumber()
                .flatMapMaybe { takeNumber ->
                    currentTakeNumberProperty.set(takeNumber)
                    audioPluginViewModel.getRecorder()
                }
                .flatMapSingle { plugin ->
                    showPluginActive = !plugin.isNativePlugin()
                    audioPluginViewModel.record(rec)
                }
                .observeOnFx()
                .doOnError { e ->
                    logger.error("Error in recording a new take", e)
                }
                .onErrorReturn { RecordTake.Result.NO_RECORDER }
                .subscribe { result: RecordTake.Result ->
                    showPluginActive = false
                    fire(PluginClosedEvent)
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
        currentTakeNumberProperty.set(editTakeEvent.take.number)
        audioPluginViewModel
            .getEditor()
            .flatMapSingle { plugin ->
                showPluginActive = !plugin.isNativePlugin()
                audioPluginViewModel.edit(editTakeEvent.take)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in editing take", e)
            }
            .onErrorReturn { EditTake.Result.NO_EDITOR }
            .subscribe { result: EditTake.Result ->
                showPluginActive = false
                currentTakeNumberProperty.set(null)
                fire(PluginClosedEvent)
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    EditTake.Result.SUCCESS -> editTakeEvent.onComplete()
                }
            }
    }

    fun markTake(markTakeEvent: MarkerTakeEvent) {
        // contextProperty.set(TakeContext.EDIT_TAKES)
        currentTakeNumberProperty.set(markTakeEvent.take.number)
        audioPluginViewModel
            .getMarker()
            .flatMapSingle { plugin ->
                showPluginActive = !plugin.isNativePlugin()
                audioPluginViewModel.mark(markTakeEvent.take)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in editing take", e)
            }
            .onErrorReturn { MarkTake.Result.NO_EDITOR }
            .subscribe { result: MarkTake.Result ->
                showPluginActive = false
                currentTakeNumberProperty.set(null)
                fire(PluginClosedEvent)
                when (result) {
                    MarkTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    MarkTake.Result.SUCCESS -> markTakeEvent.onComplete()
                }
            }
    }

    fun selectTake(take: Take?) {
        if (take != null) {
            // selectedTakeProperty will be updated when the relay emits the item that it accepts
            val found = takeCardModels.find {
                take.equals(it.take)
            }
            found?.let {
                it.selected = true
                recordable?.audio?.selectTake(it.take) ?: throw IllegalStateException("Recordable is null")
                selectedTakeProperty.set(it)
            }
        } else {
            selectedTakeProperty.set(null)
        }
    }

    fun selectTake(filename: String) {
        val take = takeCardModels.find { it.take.name == filename }
        selectTake(take?.take)
    }

    fun deleteTake(take: Take) {
        take.deletedTimestamp.accept(DateHolder.now())
    }

    fun dialogTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                String.format(
                    messages["sourceDialogTitle"],
                    currentTakeNumberProperty.get(),
                    audioPluginViewModel.pluginNameProperty.get()
                )
            },
            audioPluginViewModel.pluginNameProperty,
            currentTakeNumberProperty
        )
    }

    fun dialogTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                String.format(
                    messages["sourceDialogMessage"],
                    currentTakeNumberProperty.get(),
                    audioPluginViewModel.pluginNameProperty.get(),
                    audioPluginViewModel.pluginNameProperty.get()
                )
            },
            audioPluginViewModel.pluginNameProperty,
            currentTakeNumberProperty
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

        val takes =
            audio.getAllTakes()
                .filter { it.isNotDeleted() }
                .map { take ->
                    val ap = injector.audioPlayer
                    ap.load(take.file)
                    take.mapToCardModel(take.equals(selected))
                }

        val selectedModel = takes.find { it.selected }
        selectedTakeProperty.set(selectedModel)

        closePlayers()
        takeCardModels.setAll(takes)
        sortTakes()

        audio.takes
            .filter { it.isNotDeleted() }
            .doOnError { e ->
                logger.error("Error in loading audio takes for audio: $audio", e)
            }
            .subscribe { take ->
                if (takeCardModels.find { it.take.equals(take) } == null) {
                    val ap = injector.audioPlayer
                    ap.load(take.file)
                    addToAlternateTakes(
                        take.mapToCardModel(take.equals(selected))
                    )
                }
                removeOnDeleted(take)
            }
            .let { disposables.add(it) }
    }

    private fun removeOnDeleted(take: Take) {
        take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .doOnError { e ->
                logger.error("Error in removing deleted take: $take", e)
            }
            .subscribe {
                removeFromAlternateTakes(take)
            }
            .let { disposables.add(it) }
    }

    private fun subscribeSelectedTakePropertyToRelay(audio: AssociatedAudio) {
        audio
            .selected
            .doOnError { e ->
                logger.error("Error in subscribing take to relay for audio: $audio", e)
            }
            .subscribe { takeHolder ->
                takeCardModels.forEach { it.selected = false }
                val takeModel = takeCardModels.find { it.take == takeHolder.value }
                takeModel?.selected = true
                selectedTakeProperty.set(takeModel)
            }
            .let { disposables.add(it) }
    }

    private fun addToAlternateTakes(take: TakeCardModel) {
        Platform.runLater {
            takeCardModels.add(take)
            sortTakes()
        }
    }

    private fun sortTakes() {
        FXCollections.sort(
            takeCardModels
        ) { take1, take2 ->
            when {
                take1.selected == take2.selected -> take1.take.number.compareTo(take2.take.number)
                take1.selected -> 1
                else -> -1
            }
        }
    }

    private fun removeFromAlternateTakes(take: Take) {
        Platform.runLater {
            takeCardModels.removeAll { it.take.equals(take) }
        }
    }

    fun closePlayers() {
        takeCardModels.forEach { it.audioPlayer.close() }
    }

    fun Take.mapToCardModel(selected: Boolean): TakeCardModel {
        val ap = injector.audioPlayer
        ap.load(this.file)
        return TakeCardModel(
            this,
            selected,
            ap,
            FX.messages["edit"].capitalize(),
            FX.messages["delete"].capitalize(),
            FX.messages["play"].capitalize(),
            FX.messages["pause"].capitalize()
        )
    }
}

