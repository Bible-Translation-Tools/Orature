package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

class BlindDraftViewModel : ViewModel() {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    val sourceAudioProperty = SimpleObjectProperty<IAudioPlayer>()
    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()

    fun dockBlindDraft() {
        val wb = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        chapter
            .chunks
            .observeOnFx()
            .subscribe { chunks ->
                val list = chunks.map {
                    ChunkViewData(
                        it.sort,
                        SimpleBooleanProperty(it.hasSelectedAudio()),
                        translationViewModel.selectedChunkProperty
                    )
                }
                translationViewModel.chunkList.setAll(list)
                workbookDataStore.activeChunkProperty.set(chunks.first())
            }

//        val sourceAudio = wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
//        audioDataStore.sourceAudioProperty.set(sourceAudio)
//        audioDataStore.openSourceAudioPlayer()
        audioDataStore.updateSourceAudio()
        audioDataStore.openSourceAudioPlayer()
        sourceAudioProperty.bind(audioDataStore.sourceAudioPlayerProperty)
    }

    fun undockBlindDraft() {
        sourceAudioProperty.unbind()
        workbookDataStore.activeChunkProperty.set(null)
    }
}