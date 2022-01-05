package org.wycliffeassociates.otter.jvm.workbookapp.utils

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File

object Audio {
    fun writeWavFile(target: File) {
        val wav = AudioFile(target, 1, 44100, 16)
        val stream = Observable.just(
            // sample data
            byteArrayOf(73, -1, -18, 40, 44, 76, 92, 68, -4, 28, -91, -19, 63, 39, 93, -21, -88, -33, -19, -43, 70)
        )
        val writer = WavFileWriter(wav, stream) {}
        writer.start()
    }
}
