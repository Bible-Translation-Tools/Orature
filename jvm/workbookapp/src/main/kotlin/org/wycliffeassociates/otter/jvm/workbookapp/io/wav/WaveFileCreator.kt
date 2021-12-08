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
package org.wycliffeassociates.otter.jvm.workbookapp.io.wav

import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
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
