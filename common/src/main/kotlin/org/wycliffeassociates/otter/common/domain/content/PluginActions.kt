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
package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.wav.EMPTY_WAVE_FILE_SIZE
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.audio.wav.InvalidWavFileException
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import java.io.File
import java.lang.Exception
import java.time.LocalDate
import javax.inject.Inject

/**
 * Actions to perform on an audio file using a selected audio plugin.
 *
 * Audio plugins may be third party processes, executed with a command line interface, or dynamically linked Jars
 * that can extend the Orature application.
 *
 * Plugin Results:
 * Success - The plugin was executed successfully and the audio file was modified.
 * No Plugin - The plugin was not found, and the action could not be performed.
 * No Audio - The result of the plugin was an empty audio file.
 */
class PluginActions @Inject constructor(
    private val waveFileCreator: IWaveFileCreator,
    private val launchPlugin: LaunchPlugin
) {
    enum class Result {
        SUCCESS,
        NO_PLUGIN,
        NO_AUDIO
    }

    /**
     * Opens the selected audio editor plugin with the provided take. The audio file is expected to be edited
     * in place, and as a result, the result of audio editing must be located in the path provided by Orature in order
     * for changes to be reflected inside Orature.
     *
     * @param audio The associated audio associates with the take, and what will be used to select the take
     * after the plugin is executed
     * @param take The take to edit
     * @param pluginParameters The parameters to pass to the plugin
     *
     * @return A Single that will execute the plugin on subscribe and emits the result of the plugin execution
     */
    fun edit(audio: AssociatedAudio, take: Take, pluginParameters: PluginParameters): Single<Result> {
        return launchPlugin(PluginType.EDITOR, take, pluginParameters)
            .map { (take, result) ->
                handleModifyTake(audio::selectTake, take, result)
            }
    }

    /**
     * Opens the selected audio marker plugin with the provided take.
     *
     * Similar to editing, the audio file is expected to be modified in place.
     * @param audio The associated audio associates with the take, and what will be used to select the take
     * after the plugin is executed
     * @param take The take to add markers to
     * @param pluginParameters The parameters to pass to the plugin
     *
     * @return A Single that will execute the plugin on subscribe and emits the result of the plugin execution
     */
    fun mark(audio: AssociatedAudio, take: Take, pluginParameters: PluginParameters): Single<Result> {
        return launchPlugin(PluginType.MARKER, take, pluginParameters)
            .map { (take, result) ->
                handleModifyTake(audio::selectTake, take, result)
            }
    }

    /**
     * Creates a new audio file to record into using the selected audio recorder plugin, and adds the resulting
     * audio as a take.
     *
     * @param audio The associated audio to add the take to, as well as what will provide a new take number
     * @param projectAudioDir The directory where the audio file will be created
     * @param namer The namer to use to generate the filename of the audio file
     * @param pluginParameters The parameters to pass to the plugin
     *
     * @return A Single that will execute the plugin on subscribe and emits the result of the plugin execution
     */
    fun record(
        audio: AssociatedAudio,
        projectAudioDir: File,
        namer: FileNamer,
        pluginParameters: PluginParameters
    ): Single<Result> {
        return audio.getNewTakeNumber()
            .map { newTakeNumber ->
                val filename = namer.generateName(newTakeNumber, AudioFileFormat.WAV)
                val chapterAudioDir = getChapterAudioDirectory(
                    projectAudioDir,
                    namer.formatChapterNumber()
                )
                createNewTake(newTakeNumber, filename, chapterAudioDir, true)
            }
            .flatMap { take ->
                launchPlugin(PluginType.RECORDER, take, pluginParameters)
            }.map { (take, result) ->
                handleRecorderPluginResult(audio::insertTake, take, result)
            }
    }

    fun import(
        audio: AssociatedAudio,
        projectAudioDir: File,
        namer: FileNamer,
        take: File
    ): Completable {
        return audio.getNewTakeNumber()
            .map { newTakeNumber ->
                val format = AudioFileFormat.of(take.extension)
                val filename = namer.generateName(newTakeNumber, format)
                val chapterAudioDir = getChapterAudioDirectory(
                    projectAudioDir,
                    namer.formatChapterNumber()
                )
                writeTakeFile(chapterAudioDir, filename, take)
                createNewTake(newTakeNumber, filename, chapterAudioDir, false)
            }
            .flatMapCompletable {
                Completable.fromAction {
                    handleImportTake(audio::insertTake, it)
                }
            }
    }
    
    private fun launchPlugin(
        pluginType: PluginType,
        take: Take,
        pluginParameters: PluginParameters
    ): Single<Pair<Take, Result>> {
        return launchPlugin
            .launchPlugin(pluginType, take.file, pluginParameters)
            .map {
                when (it) {
                    LaunchPlugin.Result.SUCCESS -> Pair(take, Result.SUCCESS)
                    LaunchPlugin.Result.NO_PLUGIN -> Pair(take, Result.NO_PLUGIN)
                }
            }
    }

    private fun getChapterAudioDirectory(projectAudioDir: File, chapterNum: String): File {
        val chapterAudioDir = projectAudioDir.resolve(chapterNum)
        chapterAudioDir.mkdirs()
        return chapterAudioDir
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
            waveFileCreator.createEmpty(newTake.file)
        }
        return newTake
    }

    private fun writeTakeFile(audioDir: File, filename: String, take: File) {
        val takeFile = audioDir.resolve(File(filename))
        take.inputStream().use { input ->
            takeFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    internal fun handleRecorderPluginResult(
        insertTake: (Take) -> Unit,
        take: Take,
        result: Result
    ): Result {
        return when (result) {
            Result.SUCCESS -> {
                if (take.file.length() == EMPTY_WAVE_FILE_SIZE) {
                    take.file.delete()
                    Result.NO_AUDIO
                } else {
                    insertTake(take)
                    Result.SUCCESS
                }
            }
            Result.NO_PLUGIN, Result.NO_AUDIO -> {
                take.file.delete()
                Result.NO_PLUGIN
            }
        }
    }

    internal fun handleImportTake(insertTake: (Take) -> Unit, take: Take) {
        try {
            // Create an instance of the audio file
            // to check if it's valid
            AudioFile(take.file)
            insertTake(take)
        } catch (e: Exception) {
            take.file.delete()
            throw InvalidWavFileException("Invalid audio file")
        }
    }

    internal fun handleModifyTake(
        selectTake: (Take) -> Unit,
        take: Take,
        result: Result
    ): Result {
        take.file.setLastModified(System.currentTimeMillis())
        selectTake(take)
        return result
    }
}
