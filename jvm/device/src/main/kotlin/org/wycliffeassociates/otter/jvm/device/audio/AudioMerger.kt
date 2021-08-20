package org.wycliffeassociates.otter.jvm.device.audio

import io.reactivex.Maybe
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.device.IAudioMerger
import java.io.File

class AudioMerger : IAudioMerger {

    override fun merge(files: List<File>): Maybe<File> {
        return Maybe.fromCallable {
            if (files.isNotEmpty()) {
                val inputFile = AudioFile(files.first())
                val tempFile = File.createTempFile(
                    "output",
                    ".${inputFile.file.extension}"
                )
                tempFile.deleteOnExit()
                val outputFile = AudioFile(
                    tempFile,
                    inputFile.channels,
                    inputFile.sampleRate,
                    inputFile.bitsPerSample
                )
                outputFile.writer(append = true).use { os ->
                    files.forEach { file ->
                        os.write(file.readBytes())
                    }
                }
                outputFile.file
            } else {
                null
            }
        }
    }
}
