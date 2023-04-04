package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Single
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class SplitAudioOnCues @Inject constructor(private val directoryProvider: IDirectoryProvider) {

    fun execute(file: File): Single<Map<String, File>> {
        return Single.fromCallable {
            val sourceAudio = AudioFile(file)
            val cues = sourceAudio.metadata.getCues()
            splitAudio(file, cues)
        }
    }

    fun execute(file: File, cues: List<AudioCue>): Single<Map<String, File>> {
        return Single.fromCallable {
            splitAudio(file, cues)
        }
    }

    private fun splitAudio(file: File, cues: List<AudioCue>): Map<String, File> {
        val chunks = mutableMapOf<String, File>()
        val sourceAudio = AudioFile(file)
        val totalFrames = sourceAudio.totalFrames
        cues.forEachIndexed { index, cue ->
            val audioStartEnd = getChunkAudioRange(index, totalFrames, cues)
            val pcmFile = directoryProvider.createTempFile(index.toString(), ".${AudioFileFormat.PCM.extension}")
            val pcmAudio = AudioFile(pcmFile)
            writeAudio(sourceAudio, pcmAudio, audioStartEnd)
            chunks[cue.label] = pcmFile
        }
        return chunks
    }

    private fun writeAudio(source: AudioFile, target: AudioFile, startEnd: Pair<Int, Int>) {
        val sourceReader = source.reader(startEnd.first, startEnd.second)
        val targetWriter = target.writer()

        sourceReader.use { reader ->
            reader.open()
            targetWriter.use { writer ->
                val buffer = ByteArray(10240)
                while (reader.hasRemaining()) {
                    val written = reader.getPcmBuffer(buffer)
                    writer.write(buffer, 0, written)
                }
            }
        }
    }

    private fun getChunkAudioRange(index: Int, max: Int, cues: List<AudioCue>): Pair<Int, Int> {
        val current = cues[index].location
        val nextIndex = index + 1
        val next = if (nextIndex in 0..cues.lastIndex) {
            cues[nextIndex].location
        } else {
            max
        }

        return Pair(current, next)
    }
}