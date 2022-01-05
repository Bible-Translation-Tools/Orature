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
package org.wycliffeassociates.otter.jvm.workbookapp.utils

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File

fun writeWavFile(target: File) {
    val wav = AudioFile(target, 1, 44100, 16)
    val stream = Observable.just(
        // sample data
        byteArrayOf(73, -1, -18, 40, 44, 76, 92, 68, -4, 28, -91, -19, 63, 39, 93, -21, -88, -33, -19, -43, 70)
    )
    val writer = WavFileWriter(wav, stream) {}
    writer.start()
}
