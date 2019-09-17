package org.wycliffeassociates.otter.common.recorder

/**
 * Created by sarabiaj on 9/4/2015.
 */
class RecordingTimer {
    private var startTime: Long = 0
    private var timeStored: Long = 0
    private var paused: Boolean = true

    val timeElapsed: Long
        get() {
            val elapsed = System.currentTimeMillis() - startTime + timeStored
            return if (paused) {
                timeStored
            } else {
                elapsed
            }
        }

    fun start() {
        startTime = System.currentTimeMillis()
        paused = false
    }

    fun pause() {
        timeStored += System.currentTimeMillis() - startTime
        paused = true
    }

    fun reset() {
        timeStored = 0
    }
}
