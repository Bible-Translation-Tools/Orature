package org.wycliffeassociates.otter.common.recorder

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream

class WavFileWriter(
    private val wav: WavFile,
    private val audioStream: Observable<ByteArray>,
    private val onComplete: () -> Unit
) {
    val writer = Observable.using(
        {
            WavOutputStream(wav, append = false, buffered = true)
        },
        { writer ->
            audioStream.map {
                writer.write(it)
            }
        },
        { writer ->
            writer.close()
            onComplete()
        }
    ).subscribeOn(Schedulers.io())
        .subscribe()
}
