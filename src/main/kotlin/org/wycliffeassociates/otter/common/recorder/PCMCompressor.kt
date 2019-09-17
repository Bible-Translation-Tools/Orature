package org.wycliffeassociates.otter.common.recorder

import org.wycliffeassociates.otter.common.collections.FloatRingBuffer

class PCMCompressor(private val ringBuffer: FloatRingBuffer, samplesToCompress: Int) {

    // size is how small the waveform is being shrunk for visualization
    // arbitrary for now, could be based on number of seconds to show and resolution
    val accumulator = FloatArray(samplesToCompress)
    var index = 0

    fun add(data: FloatArray) {
        for (sample in data) {
            if (index >= accumulator.size) {
                sendDataToRingBuffer()
                index = 0
            }
            accumulator[index] = sample
            index++
        }
    }

    fun add(data: Float) {
        if (index >= accumulator.size) {
            sendDataToRingBuffer()
            index = 0
        }
        accumulator[index] = data
        index++
    }

    fun sendDataToRingBuffer() {
        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE

        for (sample in accumulator) {
            if (max < sample) max = sample
            if (min > sample) min = sample
        }
        ringBuffer.add(min)
        ringBuffer.add(max)
    }
}