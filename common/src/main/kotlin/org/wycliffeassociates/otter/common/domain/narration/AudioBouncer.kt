package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

class AudioBouncer : Runnable {
    private val queue: BlockingQueue<List<AudioMarker>> = LinkedBlockingQueue()
    private val audioBouncingTimeThreshold = 2000
    private val lock = ReentrantLock()
    private var lastBounceTime: Long = System.currentTimeMillis()

    init {
        val thread = Thread(this)
        thread.start()
    }

    override fun run() {
        while (true) {
            lock.lock()
            var mostRecentBounceState: List<AudioMarker>? = null
            val currentTime = System.currentTimeMillis()
            try {
                if (queue.size > 0 && currentTime - lastBounceTime > audioBouncingTimeThreshold) {
                    // Removes irrelevant audioBouncing requests.
                    for (i in 0 until queue.size - 1 step 1) {
                        queue.take()
                    }
                    mostRecentBounceState = queue.take()
                }
            } finally {
                // Unlocks so the user can keep pressing the undo/redo buttons (add items to the blocking queue)
                // while the bouncing is taking place.
                lock.unlock()

                mostRecentBounceState?.let {
                    // Simulates bouncing the audio
                    println("Bouncing")
                    println(mostRecentBounceState)
                    Thread.sleep(5000)
                    println("done bouncing")
                    lastBounceTime = System.currentTimeMillis()
                }
            }


        }
    }

    fun bounce(activeVerses: List<AudioMarker>) {
        lock.lock()
        try {
            queue.put(activeVerses)
            lastBounceTime = System.currentTimeMillis()
        } finally {
            lock.unlock()
        }
    }

}