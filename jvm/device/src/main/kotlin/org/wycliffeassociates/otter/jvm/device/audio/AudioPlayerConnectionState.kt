/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.device.audio

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.AudioFileReaderProvider
import java.io.File
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFileReaderProvider

internal class AudioPlayerConnectionState(
    val id: Int,
    var begin: Int? = null,
    var end: Int? = null,
    var rate: Double = 1.0,
    var position: Int = 0,
    var durationInFrames: Int = 0,
    var durationInMs: Int = 0,
    var locationInFrames: Int = 0,
    var locationInMs: Int = 0,
    val listeners: MutableList<IAudioPlayerListener> = mutableListOf()
) {
    var readerProvider: AudioFileReaderProvider? = null

    private var _reader: AudioFileReader? = null
    var reader: AudioFileReader
        set(value) {
            _reader = value
        }
        get() {
            return _reader ?: readerProvider?.getAudioFileReader() ?: throw UninitializedPropertyAccessException()
        }
}
