package org.wycliffeassociates.otter.jvm.persistence

import org.wycliffeassociates.otter.common.persistence.IWaveFileCreator
import java.io.ByteArrayInputStream
import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class WaveFileCreator : IWaveFileCreator {
    override fun createEmpty(path: File) {
        AudioSystem.write(
                AudioInputStream(
                        ByteArrayInputStream(ByteArray(0)),
                        AudioFormat(
                                44100.0f,
                                16,
                                1,
                                true,
                                false
                        ),
                        0
                ),
                AudioFileFormat.Type.WAVE,
                path
        )
    }
}