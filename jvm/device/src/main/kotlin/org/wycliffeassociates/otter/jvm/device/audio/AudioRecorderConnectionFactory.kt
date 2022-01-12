/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import com.jakewharton.rxrelay2.PublishRelay
import javax.sound.sampled.TargetDataLine
import org.wycliffeassociates.otter.common.device.IAudioRecorder

internal class AudioRecorderConnectionFactory(
    private val errorRelay: PublishRelay<AudioError> = PublishRelay.create()
) {

    private var inputLine: TargetDataLine? = null

    @Synchronized
    fun setLine(newLine: TargetDataLine?) {
        inputLine = newLine
    }

    fun getRecorder(): IAudioRecorder {
        return AudioRecorder(inputLine, errorRelay)
    }
}
