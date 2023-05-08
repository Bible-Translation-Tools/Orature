/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.TargetAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.booleanBinding
import tornadofx.onChange
import java.io.File
import javax.inject.Inject

class AudioDataStore : Component(), ScopedInstance {
    private val logger = LoggerFactory.getLogger(AudioDataStore::class.java)

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    val workbookDataStore: WorkbookDataStore by inject()

    val selectedChapterPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)
    val sourceAudioProperty = SimpleObjectProperty<SourceAudio>()
    val sourceAudioAvailableProperty = sourceAudioProperty.booleanBinding { it?.file?.exists() ?: false }
    val targetAudioProperty = SimpleObjectProperty<TargetAudio>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        workbookDataStore.activeChapterProperty.onChange {
            logger.info("Active chapter: ${it?.sort}")
            if (it == null) cleanUpTargetAudio()
            updateSourceAudio()
        }
        workbookDataStore.activeChunkProperty.onChangeAndDoNow {
            logger.info("Active chunk: ${it?.sort}")
            updateSourceAudio()
        }
    }

    fun openPlayers() {
        targetAudioProperty.value?.let { target ->
            target.player.load(target.file)
        }
        sourceAudioProperty.value?.let { source ->
            val audioPlayer = getNewAudioPlayer()
            audioPlayer.loadSection(source.file, source.start, source.end)
            sourceAudioPlayerProperty.set(audioPlayer)
        }
    }

    fun openSourceAudioPlayer() {
        sourceAudioProperty.value?.let { source ->
            val audioPlayer = getNewAudioPlayer()
            audioPlayer.loadSection(source.file, source.start, source.end)
            sourceAudioPlayerProperty.set(audioPlayer)
        } ?: sourceAudioPlayerProperty.set(null)
    }

    fun closeSourceAudioPlayer() {
        sourceAudioPlayerProperty.value?.close()
    }

    fun stopSourceAudioPlayer() {
        sourceAudioPlayerProperty.value?.stop()
    }

    fun openTargetAudioPlayer() {
        targetAudioProperty.value?.let { target ->
            target.player.load(target.file)
        }
    }

    fun closePlayers() {
        selectedChapterPlayerProperty.value?.close()
        targetAudioProperty.value?.player?.close()
        sourceAudioPlayerProperty.value?.close()
        sourceAudioPlayerProperty.set(null)
    }

    fun stopPlayers() {
        sourceAudioPlayerProperty.value?.stop()
        targetAudioProperty.value?.player?.stop()
    }

    fun updateSelectedChapterPlayer() {
        val chunk = workbookDataStore.activeChunkProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        when {
            chapter != null && chunk == null -> {
                val take = chapter.audio.selected.value?.value
                take?.let {
                    updateTargetAudio(it.file)

                    val audioPlayer = getNewAudioPlayer()
                    audioPlayer.load(it.file)
                    selectedChapterPlayerProperty.set(audioPlayer)
                } ?: run {
                    selectedChapterPlayerProperty.set(null)
                    targetAudioProperty.set(null)
                }
            }
            chapter != null -> { /* no-op */
            } // preserve targetAudio for clean up
            else -> {
                selectedChapterPlayerProperty.set(null)
                targetAudioProperty.set(null)
            }
        }
    }

    private fun updateTargetAudio(file: File) {
        cleanUpTargetAudio()

        val tempFile = directoryProvider.createTempFile(
            file.nameWithoutExtension,
            ".${file.extension}"
        )
        file.copyTo(tempFile, true)

        val audioPlayer = getNewAudioPlayer()
        audioPlayer.load(tempFile)
        val targetAudio = TargetAudio(tempFile, audioPlayer)

        targetAudioProperty.set(targetAudio)
    }

    private fun cleanUpTargetAudio() {
        targetAudioProperty.value?.let {
            it.player.release()
            it.file.delete()
        }
        targetAudioProperty.set(null)
    }

    fun updateSourceAudio() {
        workbookDataStore.activeWorkbookProperty.value?.let { workbook ->
            val chunk = workbookDataStore.activeChunkProperty.get()
            val chapter = workbookDataStore.activeChapterProperty.get()
            if (chapter != null && chunk != null) {
                sourceAudioProperty.set(workbook.sourceAudioAccessor.getChunk(chapter.sort, chunk.sort, workbook.target))
            } else if (chapter != null) {
                sourceAudioProperty.set(workbook.sourceAudioAccessor.getChapter(chapter.sort, workbook.target))
            } else {
                sourceAudioProperty.set(null)
            }
        } ?: run {
            sourceAudioProperty.set(null)
        }
    }

    fun getSourceAudio(): SourceAudio? {
        val workbook = workbookDataStore.workbook
        val chunk = workbookDataStore.chunk
        val chapter = workbookDataStore.chapter
        val sourceAudio = workbook.sourceAudioAccessor
        val meta = workbook.target
        return chunk?.let { _chunk ->
            sourceAudio.getChunk(
                chapter.sort,
                _chunk.sort,
                meta
            )
        } ?: run {
            sourceAudio.getChapter(chapter.sort, meta)
        }
    }

    private fun getNewAudioPlayer(): IAudioPlayer {
        return (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
    }
}