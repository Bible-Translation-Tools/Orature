package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.PluginParameters
import org.wycliffeassociates.otter.common.data.model.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.io.wav.EMPTY_WAVE_FILE_SIZE
import org.wycliffeassociates.otter.common.io.wav.IWaveFileCreator
import java.io.File
import java.time.LocalDate

class RecordTake(
    private val waveFileCreator: IWaveFileCreator,
    private val launchPlugin: LaunchPlugin
) {
    enum class Result {
        SUCCESS,
        NO_RECORDER,
        NO_AUDIO
    }

    fun record(
        audio: AssociatedAudio,
        projectAudioDir: File,
        namer: FileNamer,
        pluginParameters: PluginParameters
    ): Single<Result> {
        return audio.getNewTakeNumber()
            .map { newTakeNumber ->
                val filename = namer.generateName(newTakeNumber)
                val chapterAudioDir = getChapterAudioDirectory(projectAudioDir, namer.formatChapterNumber())
                create(newTakeNumber, filename, chapterAudioDir)
            }
            .flatMap {
                doLaunchPlugin(audio::insertTake, it, pluginParameters)
            }
    }

    private fun getChapterAudioDirectory(projectAudioDir: File, chapterNum: String): File {
        val chapterAudioDir = projectAudioDir.resolve(chapterNum)
        chapterAudioDir.mkdirs()
        return chapterAudioDir
    }

    private fun create(
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

    private fun doLaunchPlugin(
        insertTake: (Take) -> Unit,
        take: Take,
        pluginParameters: PluginParameters
    ): Single<Result> = launchPlugin
        .launchRecorder(take.file, pluginParameters)
        .map {
            handlePluginResult(insertTake, take, it)
        }

    internal fun handlePluginResult(insertTake: (Take) -> Unit, take: Take, result: LaunchPlugin.Result): Result {
        return when (result) {
            LaunchPlugin.Result.SUCCESS -> {
                if (take.file.length() == EMPTY_WAVE_FILE_SIZE) {
                    take.file.delete()
                    Result.NO_AUDIO
                } else {
                    insertTake(take)
                    Result.SUCCESS
                }
            }
            LaunchPlugin.Result.NO_PLUGIN -> {
                take.file.delete()
                Result.NO_RECORDER
            }
        }
    }
}
