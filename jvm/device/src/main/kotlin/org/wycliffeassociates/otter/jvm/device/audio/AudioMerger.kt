package org.wycliffeassociates.otter.jvm.device.audio

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.device.IAudioMerger
import java.io.File

class AudioMerger : IAudioMerger {

    override fun merge(files: List<File>, output: File): Completable {
        return Completable.fromCallable {
            if (files.isNotEmpty()) {
                val inputFile = AudioFile(files.first())
                val outputFile = AudioFile(
                    output,
                    inputFile.channels,
                    inputFile.sampleRate,
                    inputFile.bitsPerSample
                )
                files.forEach { file ->
                    outputFile.writer(append = true).use {
                        it.write(file.readBytes())
                    }
                }
            }
        }
    }
}
