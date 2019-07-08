package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.*
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordScriptureViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val takeManagementViewModel: TakeManagementViewModel by inject()

    // This will be bidirectionally bound to workbookViewModel's activeChunkProperty
    private val activeChunkProperty = SimpleObjectProperty<Chunk?>()
    private var activeChunk by activeChunkProperty

    private val selectedTakeProperty = SimpleObjectProperty<Take?>()
    var selectedTake by selectedTakeProperty

    private var context: TakeContext by property(TakeContext.RECORD)
    val contextProperty = getProperty(RecordScriptureViewModel::context)

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property()
    val titleProperty = getProperty(RecordScriptureViewModel::title)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(RecordScriptureViewModel::showPluginActive)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val chunkList: ObservableList<Chunk> = observableList()
    val hasNext = SimpleBooleanProperty(false)
    val hasPrevious = SimpleBooleanProperty(false)

    init {
        activeChunkProperty.bindBidirectional(workbookViewModel.activeChunkProperty)

        workbookViewModel.activeChapterProperty.onChangeAndDoNow {
            it?.let { chapter -> getChunkList(chapter.chunks) }
        }

        activeChunkProperty.onChange {
            it?.let { chunk ->
                setTitle(chunk)
                populateTakes(chunk.audio)
            }
        }
        reset()
    }

    private fun setTitle(chunk: Chunk) {
        val label = ContentLabel.VERSE.value
        val start = chunk.start
        title = "$label $start"
    }

    private fun getChunkList(chunks: Observable<Chunk>) {
        chunks.toList()
            .observeOnFx()
            .subscribe { list ->
                chunkList.setAll(list.sortedBy { chunk -> chunk.start })
                enableButtons()
            }
    }

    private fun populateTakes(audio: AssociatedAudio) {
        selectedTake = audio.selected.value?.value
        audio.takes
            .toList()
            .observeOnFx()
            .subscribe { retrievedTakes ->
                retrievedTakes
                    .filter { it != selectedTake }
                    .let { alternateTakes.setAll(it) }
            }
    }

    fun selectTake(audio: AssociatedAudio, take: Take) {
        audio.selectTake(take)

        // Move the old selected take back to the alternates (if not null)
        selectedTake?.let {
            alternateTakes.add(it)
        }

        // Set the new selected take value
        selectedTake = take

        // Remove the new selected take from the alternates
        alternateTakes.remove(take)
    }

    fun editContent(take: Take) {
        contextProperty.set(TakeContext.EDIT_TAKES)
        showPluginActive = true
        takeManagementViewModel.edit(take)
            .observeOnFx()
            .subscribe { result ->
                showPluginActive = false
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    else -> {}
                }
            }
    }

    fun nextVerse() {
        val nextVerse = chunkList.find { verse ->
            verse.start == activeContent.start + 1
        } ?: activeContent
        if (nextVerse != null) {
            activeContentProperty.set(nextVerse)
            populateTakes(nextVerse)
        }
    }

    fun previousVerse() {
        val previousVerse = chunkList.find {
            it.start == activeContent.start - 1 && it.type != ContentType.META //don't pull chapter/meta
        }
                ?: activeContent
        if (previousVerse != null) {
            activeContentProperty.set(previousVerse)
            populateTakes(previousVerse)
        }
    }

    fun delete(take: Take) {
        if (take == selectedTakeProperty.value) {
            // Delete the selected take
            accessTakes
                    .setSelectedTake(activeContentProperty.value, null)
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
        activeContentProperty.value?.let { populateTakes(it) }
        title = if (activeContentProperty.value?.type == ContentType.META) {
            // TODO
            "TODO"
//            activeCollectionProperty.value?.titleKey ?: ""
        } else {
            val label = FX.messages[activeContentProperty.value?.labelKey ?: ContentLabel.VERSE.value]
            val start = activeContentProperty.value?.start ?: ""
            "$label $start"
        }
    }

    private fun enableButtons() {
        if(chunkList.size != 0) {
            if (activeContent != null) {
                hasNext.set(activeContent.start < chunkList.last().start)
                hasPrevious.set(activeContent.start > chunkList.first().start)
            }
        }
    }

    fun recordContent(recordable: Recordable) {
        contextProperty.set(TakeContext.RECORD)
        activeProjectProperty.value?.let { project ->
            showPluginActive = true
            takeManagementViewModel
                .record(recordable)
                .observeOnFx()
                .doOnSuccess { result ->
                    showPluginActive = false
                    when (result) {
                        RecordTake.Result.SUCCESS -> {
                            populateTakes(activeContentProperty.value)
                        }

                        RecordTake.Result.NO_RECORDER -> snackBarObservable.onNext(messages["noRecorder"])
                        RecordTake.Result.NO_AUDIO -> {
                        }
                    }
                }
                .toCompletable()
                .onErrorResumeNext {
                    Completable.fromAction {
                        showPluginActive = false
                        snackBarObservable.onNext(messages["noRecorder"])
                    }
                }
                .subscribe()
        }
    }

}