package org.wycliffeassociates.otter.common.domain.audio

import io.reactivex.Completable
import java.io.File
import de.sciss.jump3r.Main as jump3r

class AudioConverter {
    fun wavToMp3(
        wavFile: File,
        mp3File: File,
        bitrate: String,
        delete: Boolean
    ): Completable {
        return Completable.fromCallable {
            val args = arrayOf(
                "-b", bitrate,
                "-m", "m",
                wavFile.invariantSeparatorsPath,
                mp3File.invariantSeparatorsPath
            )
            jump3r().run(args)

            if (delete) {
                wavFile.delete()
            }
        }
    }
}