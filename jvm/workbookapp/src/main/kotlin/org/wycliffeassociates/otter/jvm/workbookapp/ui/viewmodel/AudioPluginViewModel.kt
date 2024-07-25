/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import tornadofx.*
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class AudioPluginViewModel : ViewModel() {
    @Inject lateinit var pluginRepository: IAudioPluginRepository
    @Inject lateinit var launchPlugin: LaunchPlugin
    @Inject lateinit var pluginActions: PluginActions
    @Inject lateinit var localeLanguage: LocaleLanguage

    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioDataStore: AudioDataStore by inject()
    private val appPreferencesStore: AppPreferencesStore by inject()
    private val settingsViewModel: SettingsViewModel by inject()

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

    fun record(recordable: Recordable): Single<PluginActions.Result> {
        val params = constructPluginParameters()
        return pluginActions.record(
            audio = recordable.audio,
            projectAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir,
            namer = createFileNamer(recordable),
            pluginParameters = params
        )
    }

    fun record(take: Take): Single<PluginActions.Result> {
        val params = constructPluginParameters()
        return pluginActions.record(take, params)
    }

    fun import(recordable: Recordable, take: File): Completable {
        return pluginActions.import(
            audio = recordable.audio,
            projectAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir,
            namer = createFileNamer(recordable),
            take = take
        )
    }

    private fun constructPluginParameters(action: String = ""): PluginParameters {
        val workbook = workbookDataStore.workbook
        val sourceAudio = audioDataStore.getSourceAudio()
        val sourceText = workbookDataStore.sourceTextBinding().value

        val chapterLabel = messages[workbookDataStore.activeChapterProperty.value.label]
        val chapterNumber = workbookDataStore.activeChapterProperty.value.sort

        val verseLabels = workbookDataStore.getSourceChapter()
            .map { it.getDraft() }.blockingGet()
            .filter { it.contentType == ContentType.TEXT }
            .map { it.title }.toList()
            .blockingGet()

        val verseTotal =  workbookDataStore.getSourceChapter().flatMapSingle { it.chunkCount }.blockingGet()

        val chunkLabel = workbookDataStore.activeChunkProperty.value?.let {
            messages[workbookDataStore.activeChunkProperty.value.label]
        }
        val chunkNumber = workbookDataStore.activeChunkProperty.value?.sort
        val chunkTitle = workbookDataStore.activeChunkProperty.value?.title
        val resourceLabel = workbookDataStore.activeResourceComponentProperty.value?.let {
            messages[workbookDataStore.activeResourceComponentProperty.value.label]
        }
        val targetAudio = audioDataStore.targetAudioProperty.value

        val sourceRate = (workbookDataStore.workbook.translation.sourceRate as BehaviorRelay).value ?: 1.0
        val targetRate = (workbookDataStore.workbook.translation.targetRate as BehaviorRelay).value ?: 1.0
        val sourceTextZoom = appPreferencesStore.sourceTextZoomRateProperty.value

        return PluginParameters(
            languageName = workbook.target.language.name,
            bookSlug = workbook.target.slug,
            bookTitle = workbook.target.title,
            chapterLabel = chapterLabel,
            chapterNumber = chapterNumber,
            verseLabels = verseLabels,
            verseTotal = verseTotal,
            chunkLabel = chunkLabel,
            chunkNumber = chunkNumber,
            chunkTitle = chunkTitle,
            resourceLabel = resourceLabel,
            sourceChapterAudio = sourceAudio?.file,
            sourceChunkStart = sourceAudio?.start,
            sourceChunkEnd = sourceAudio?.end,
            sourceText = sourceText,
            actionText = action,
            targetChapterAudio = targetAudio?.file,
            license = workbook.source.resourceMetadata.license,
            direction = localeLanguage.preferredLanguage?.direction,
            sourceDirection = workbook.source.language.direction,
            sourceRate = sourceRate,
            targetRate = targetRate,
            sourceTextZoom = sourceTextZoom,
            sourceLanguageName = workbook.source.language.name
        )
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

    /**
     * This method opens the given file in the plugin and returns the result.
     * It uses dummy AssociatedAudio, that doesn't insert/update the record in the database
     */
    fun edit(file: File): Single<PluginActions.Result> {
        val takes = ReplayRelay.create<Take>()
        val audio = AssociatedAudio(takes)
        val take = Take(
            name = file.name,
            file = file,
            number = 1,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now()
        )
        return edit(audio, take)
    }

    fun edit(audio: AssociatedAudio, take: Take): Single<PluginActions.Result> {
        val params = constructPluginParameters()
        return pluginActions.edit(audio, take, params)
    }

    fun mark(audio: AssociatedAudio, take: Take): Single<PluginActions.Result> {
        val params = constructPluginParameters(messages["markAction"])
        return pluginActions.mark(audio, take, params)
    }

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginDialog>().apply {
            themeProperty.set(settingsViewModel.appColorMode.value)
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            open()
        }
        find<AddPluginViewModel>().apply {
            canRecordProperty.value = record
            canEditProperty.value = edit
        }
    }
}
