package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.audio.wav.EMPTY_WAVE_FILE_SIZE
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class TakeActions @Inject constructor(
    private val waveFileCreator: IWaveFileCreator,
    private val launchPlugin: LaunchPlugin
) {
    enum class Result {
        SUCCESS,
        NO_PLUGIN,
        NO_AUDIO
    }

    fun edit(plugin: IAudioPlugin, take: Take, pluginParameters: PluginParameters): Single<Result> {
        return launchPlugin(plugin, take, pluginParameters).map { (t, r) -> r }
    }

    fun mark(plugin: IAudioPlugin, take: Take, pluginParameters: PluginParameters): Single<Result> {
        return launchPlugin(plugin, take, pluginParameters).map { (t, r) -> r }
    }

    fun record(
        plugin: IAudioPlugin,
        audio: AssociatedAudio,
        projectAudioDir: File,
        namer: FileNamer,
        pluginParameters: PluginParameters
    ): Single<Result> {
        return audio.getNewTakeNumber()
            .map { newTakeNumber ->
                val filename = namer.generateName(newTakeNumber)
                val chapterAudioDir = getChapterAudioDirectory(
                    projectAudioDir,
                    namer.formatChapterNumber()
                )
                createNewTake(newTakeNumber, filename, chapterAudioDir)
            }
            .flatMap { take ->
                launchPlugin(plugin, take, pluginParameters)
            }.map { (take, result) ->
                handleRecorderPluginResult(audio::insertTake, take, result)
            }
    }

    private fun launchPlugin(
        plugin: IAudioPlugin,
        take: Take,
        pluginParameters: PluginParameters
    ): Single<Pair<Take, Result>> {
        return launchPlugin
            .launchPlugin(plugin, take.file, pluginParameters)
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
        audioDir: File
    ): Take {
        val takeFile = audioDir.resolve(File(filename))

        val newTake = Take(
            name = takeFile.name,
            file = takeFile,
            number = newTakeNumber,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now()
        )
        waveFileCreator.createEmpty(newTake.file)
        return newTake
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
}
