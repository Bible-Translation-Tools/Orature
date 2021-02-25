package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Maybe
import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javax.inject.Inject
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.AddPluginView
import tornadofx.*

class AudioPluginViewModel : ViewModel() {
    @Inject lateinit var pluginRepository: IAudioPluginRepository
    @Inject lateinit var launchPlugin: LaunchPlugin
    @Inject lateinit var takeActions: TakeActions

    private val workbookViewModel: WorkbookViewModel by inject()

    val pluginNameProperty = SimpleStringProperty()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedMarkerProperty = SimpleObjectProperty<AudioPluginData>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun getPlugin(pluginType: PluginType): Maybe<IAudioPlugin> {
        return pluginRepository.getPlugin(pluginType)
    }

    fun record(recordable: Recordable): Single<TakeActions.Result> {
        val params = constructPluginParameters()
        return takeActions.record(
            audio = recordable.audio,
            projectAudioDir = workbookViewModel.activeProjectFilesAccessor.audioDir,
            namer = createFileNamer(recordable),
            pluginParameters = params
        )
    }

    private fun constructPluginParameters(action: String = ""): PluginParameters {
        val workbook = workbookViewModel.workbook
        val sourceAudio = workbookViewModel.getSourceAudio()
        val sourceText = workbookViewModel.getSourceText().blockingGet()

        val chapterLabel = messages[workbookViewModel.activeChapterProperty.value.label]
        val chapterNumber = workbookViewModel.activeChapterProperty.value.sort
        val verseTotal = workbookViewModel.activeChapterProperty.value.chunks.blockingLast().end
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
            verseTotal = verseTotal,
            chunkLabel = chunkLabel,
            chunkNumber = chunkNumber,
            resourceLabel = resourceLabel,
            sourceChapterAudio = sourceAudio?.file,
            sourceChunkStart = sourceAudio?.start,
            sourceChunkEnd = sourceAudio?.end,
            sourceText = sourceText,
            actionText = action
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

    fun edit(take: Take): Single<TakeActions.Result> {
        val params = constructPluginParameters()
        return takeActions.edit(take, params)
    }

    fun mark(take: Take): Single<TakeActions.Result> {
        val params = constructPluginParameters(messages["markAction"])
        return takeActions.mark(take, params)
    }

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginView>().openModal()
    }
}
