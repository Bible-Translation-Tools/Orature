package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
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
                workbookDataStore.activeChunkProperty.set(chunks.first())
            }

        audioDataStore.updateSourceAudio()
        audioDataStore.openSourceAudioPlayer()
        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        loadSourceMarkers(OratureAudioFile(audioDataStore.sourceAudioProperty.value.file))
    }

    fun undockBlindDraft() {
        sourcePlayerProperty.unbind()
        workbookDataStore.activeChunkProperty.set(null)
        markerModelProperty.set(null)
        translationViewModel.currentMarkerProperty.set(-1)
    }

    fun loadSourceMarkers(audio: OratureAudioFile) {
        audio.clearCues()
        val verseMarkers = audio.getMarker<VerseMarker>()
        val markerModel = VerseMarkerModel(audio, verseMarkers.size, verseMarkers.map { it.label })
        markerModelProperty.set(markerModel)
    }
}