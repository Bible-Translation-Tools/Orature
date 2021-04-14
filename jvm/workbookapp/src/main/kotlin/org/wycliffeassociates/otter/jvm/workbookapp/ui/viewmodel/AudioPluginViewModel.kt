package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Maybe
import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import tornadofx.*
import javax.inject.Inject

class AudioPluginViewModel : ViewModel() {
    @Inject lateinit var pluginRepository: IAudioPluginRepository
    @Inject lateinit var launchPlugin: LaunchPlugin
    @Inject lateinit var takeActions: TakeActions

    private val workbookDataStore: WorkbookDataStore by inject()

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

    fun record(plugin: IAudioPlugin, recordable: Recordable): Single<TakeActions.Result> {
        val params = constructPluginParameters()
        return takeActions.record(
            plugin = plugin,
            audio = recordable.audio,
            projectAudioDir = workbookDataStore.activeProjectFilesAccessor.audioDir,
            namer = createFileNamer(recordable),
            pluginParameters = params
        )
    }

    private fun constructPluginParameters(action: String = ""): PluginParameters {
        val workbook = workbookDataStore.workbook
        val sourceAudio = workbookDataStore.getSourceAudio()
        val sourceText = workbookDataStore.getSourceText().blockingGet()

        val chapterLabel = messages[workbookDataStore.activeChapterProperty.value.label]
        val chapterNumber = workbookDataStore.activeChapterProperty.value.sort
        val verseTotal = workbookDataStore.activeChapterProperty.value.chunks.blockingLast().end
        val chunkLabel = workbookDataStore.activeChunkProperty.value?.let {
            messages[workbookDataStore.activeChunkProperty.value.label]
        }
        val chunkNumber = workbookDataStore.activeChunkProperty.value?.sort
        val resourceLabel = workbookDataStore.activeResourceComponentProperty.value?.let {
            messages[workbookDataStore.activeResourceComponentProperty.value.label]
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
            workbook = workbookDataStore.workbook,
            chapter = workbookDataStore.chapter,
            chunk = workbookDataStore.chunk,
            recordable = recordable,
            rcSlug = workbookDataStore.activeResourceMetadata.identifier
        )
    }

    fun edit(plugin: IAudioPlugin, take: Take): Single<TakeActions.Result> {
        val params = constructPluginParameters()
        return takeActions.edit(plugin, take, params)
    }

    fun mark(plugin: IAudioPlugin, take: Take): Single<TakeActions.Result> {
        val params = constructPluginParameters(messages["markAction"])
        return takeActions.mark(plugin, take, params)
    }

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginDialog>().openModal()
    }
}
