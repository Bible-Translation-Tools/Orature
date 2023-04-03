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
package org.wycliffeassociates.otter.common.recorder

import io.reactivex.subjects.PublishSubject

class PCMCompressor(
    samplesToCompress: Int,
    private val dataSender: PublishSubject<Float>
) {

    // size is how small the waveform is being shrunk for visualization
    // arbitrary for now, could be based on number of seconds to show and resolution
    private val accumulator = FloatArray(samplesToCompress)
    private var index = 0

    fun add(data: FloatArray) {
        for (sample in data) {
            if (index >= accumulator.size) {
                sendData()
                index = 0
            }
            accumulator[index] = sample
            index++
        }
    }

    fun add(data: Float) {
        if (index >= accumulator.size) {
            sendData()
            index = 0
        }
        accumulator[index] = data
        index++
    }

    private fun sendData() {
        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE

        for (sample in accumulator) {
            if (max < sample) max = sample
            if (min > sample) min = sample
        }

        dataSender.onNext(min)
        dataSender.onNext(max)
    }
}
