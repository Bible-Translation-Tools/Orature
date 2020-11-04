package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel

import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.PluginParameters
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.*
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.workbookapp.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.io.wav.WaveFileCreator
import tornadofx.*

class AudioPluginViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val pluginRepository = injector.pluginRepository

    private val workbookViewModel: WorkbookViewModel by inject()

    private val launchPlugin = LaunchPlugin(pluginRepository)
    private val recordTake = RecordTake(WaveFileCreator(), launchPlugin)
    private val editTake = EditTake(launchPlugin)
    private val markTake = MarkTake(launchPlugin)

    fun getRecorder() = pluginRepository.getRecorder()
    fun getEditor() = pluginRepository.getEditor()
    fun getMarker() = pluginRepository.getMarker()

    val pluginNameProperty = SimpleStringProperty()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()

    fun record(recordable: Recordable): Single<RecordTake.Result> {
        val params = constructPluginParameters()
        return recordTake.record(
            audio = recordable.audio,
            projectAudioDir = workbookViewModel.activeProjectAudioDirectory,
            namer = createFileNamer(recordable),
            pluginParameters = params
        )
    }

    private fun constructPluginParameters(): PluginParameters {
        val workbook = workbookViewModel.workbook
        val sourceAudio = workbookViewModel.getSourceAudio()
        val sourceText = workbookViewModel.getSourceText().blockingGet()

        val chapterLabel = messages[workbookViewModel.activeChapterProperty.value.label]
        val chapterNumber = workbookViewModel.activeChapterProperty.value.sort
        val chunkLabel = workbookViewModel.activeChunkProperty.value?.let {
            messages[workbookViewModel.activeChunkProperty.value.label]
        }
        val chunkNumber = workbookViewModel.activeChunkProperty.value?.sort
        val resourceLabel = workbookViewModel.activeResourceComponentProperty.value?.let {
            messages[workbookViewModel.activeResourceComponentProperty.value.label]
        }

        return PluginParameters(
            languageName = workbook.target.language.name,
            bookTitle = workbook.target.title,
            chapterLabel = chapterLabel,
            chapterNumber = chapterNumber,
            chunkLabel = chunkLabel,
            chunkNumber = chunkNumber,
            resourceLabel = resourceLabel,
            sourceChapterAudio = sourceAudio?.file,
            sourceChunkStart = sourceAudio?.start,
            sourceChunkEnd = sourceAudio?.end,
            sourceText = sourceText
        )
    }

    private fun createFileNamer(recordable: Recordable): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbookViewModel.workbook,
            chapter = workbookViewModel.chapter,
            chunk = workbookViewModel.chunk,
            recordable = recordable,
            rcSlug = workbookViewModel.activeResourceMetadata.identifier
        )
    }

    fun edit(take: Take): Single<EditTake.Result> {
        val params = constructPluginParameters()
        return editTake.edit(take, params)
    }

    fun mark(take: Take): Single<MarkTake.Result> {
        val params = constructPluginParameters()
        return markTake.mark(take, params)
    }

    fun audioPlayer(): IAudioPlayer = injector.audioPlayer

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginView>().openModal()
    }
}
