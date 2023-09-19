package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

class BlindDraftViewModel : ViewModel() {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val markerModelProperty = SimpleObjectProperty<VerseMarkerModel>()

    fun dockBlindDraft() {
        val chapter = workbookDataStore.chapter
        chapter
            .chunks
            .observeOnFx()
            .subscribe { chunks ->
                val list = chunks.map {
                    ChunkViewData(
                        it.sort,
                        SimpleBooleanProperty(it.hasSelectedAudio()),
                        translationViewModel.selectedChunkBinding
                    )
                }
                translationViewModel.chunkList.setAll(list)
                chunks.firstOrNull { !it.hasSelectedAudio()}
                    ?.let { translationViewModel.selectChunk(it.sort) }
            }

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
    }

    fun undockBlindDraft() {
        sourcePlayerProperty.unbind()
        workbookDataStore.activeChunkProperty.set(null)
        markerModelProperty.set(null)
        translationViewModel.updateSourceText()
    }
}