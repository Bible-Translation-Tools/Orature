package org.bibletranslationtools.vtt

import java.util.*
import kotlin.math.min


internal class WebvttSubtitle(cueInfos: List<WebvttCueInfo>) : Subtitle {
    private val cueInfos: List<WebvttCueInfo>
    private val cueTimesUs: LongArray
    private val sortedCueTimesUs: LongArray

    /** Constructs a new WebvttSubtitle from a list of [WebvttCueInfo]s.  */
    init {
        this.cueInfos = cueInfos.map { it }
        cueTimesUs = LongArray(2 * cueInfos.size)
        for (cueIndex in cueInfos.indices) {
            val cueInfo = cueInfos[cueIndex]
            val arrayIndex = cueIndex * 2
            cueTimesUs[arrayIndex] = cueInfo.startTimeUs
            cueTimesUs[arrayIndex + 1] = cueInfo.endTimeUs
        }
        sortedCueTimesUs = cueTimesUs.copyOf(cueTimesUs.size)
        Arrays.sort(sortedCueTimesUs)
    }

    override fun getNextEventTimeIndex(timeUs: Long): Int {
        val index: Int = binarySearchCeil(sortedCueTimesUs, timeUs, false, false)
        return if (index < sortedCueTimesUs.size) index else INDEX_UNSET
    }

    private fun binarySearchCeil(
        array: LongArray, value: Long, inclusive: Boolean, stayInBounds: Boolean
    ): Int {
        var index = Arrays.binarySearch(array, value)
        if (index < 0) {
            index = index.inv()
        } else {
            while (++index < array.size && array[index] == value) {
            }
            if (inclusive) {
                index--
            }
        }
        return if (stayInBounds) min(array.size - 1, index) else index
    }

    override fun getEventTimeCount(): Int {
        return sortedCueTimesUs.size
    }

    override fun getEventTime(index: Int): Long {
        check(index >= 0)
        check(index < sortedCueTimesUs.size)
        return sortedCueTimesUs[index]
    }

    override fun getCues(timeUs: Long): List<Cue> {
        val currentCues: MutableList<Cue> = ArrayList()
        for (i in cueInfos.indices) {
            if ((cueTimesUs[i * 2] <= timeUs) && (timeUs < cueTimesUs[i * 2 + 1])) {
                val cueInfo = cueInfos[i]
                currentCues.add(cueInfo.cue)
            }
        }
        return currentCues
    }
}
