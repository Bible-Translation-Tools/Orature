package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.workbookapp.io.wav.WaveFileCreator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*
import java.io.File
import java.time.LocalDate

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

    fun newTakeFile(): Single<Take> {
        return workbookDataStore.chunk!!.let { chunk ->
            val namer = createFileNamer(chunk)
            val chapterAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir.resolve(namer.formatChapterNumber())
            chunk.audio.getNewTakeNumber()
                .map { takeNumber ->
                    createNewTake(
                        takeNumber,
                        namer.generateName(takeNumber, AudioFileFormat.WAV),
                        chapterAudioDir,
                        true
                    )
                }
        }
    }

    private fun createNewTake(
        newTakeNumber: Int,
        filename: String,
        audioDir: File,
        createEmpty: Boolean
    ): Take {
        val takeFile = audioDir.resolve(File(filename))
        val newTake = Take(
            name = takeFile.name,
            file = takeFile,
            number = newTakeNumber,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now()
        )
        if (createEmpty) {
//            waveFileCreator.createEmpty(newTake.file)
            WaveFileCreator().createEmpty(newTake.file)
        }
        return newTake
    }

    private fun createFileNamer(recordable: Recordable): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbookDataStore.workbook,
            chapter = workbookDataStore.chapter,
            chunk = workbookDataStore.chunk,
            recordable = recordable,
            rcSlug = workbookDataStore.workbook.sourceMetadataSlug
        )
    }
}