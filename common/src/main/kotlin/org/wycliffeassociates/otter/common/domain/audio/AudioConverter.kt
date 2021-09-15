/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.audio

import io.reactivex.Completable
import java.io.File
import de.sciss.jump3r.Main as jump3r

class AudioConverter {
    fun wavToMp3(
        wavFile: File,
        mp3File: File,
        bitrate: Int = 64
    ): Completable {
        return Completable.fromCallable {
            val args = arrayOf(
                "-b", bitrate.toString(),
                "-m", "m",
                wavFile.invariantSeparatorsPath,
                mp3File.invariantSeparatorsPath
            )
            jump3r().run(args)
        }
    }
}