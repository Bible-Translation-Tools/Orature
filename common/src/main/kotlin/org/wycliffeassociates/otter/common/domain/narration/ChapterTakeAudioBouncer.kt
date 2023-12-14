package org.wycliffeassociates.otter.common.domain.narration

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

class ChapterTakeAudioBouncer : Runnable {
    private val logger = LoggerFactory.getLogger(ChapterTakeAudioBouncer::class.java)

    var file: File? = null
    var reader: AudioFileReader? = null
    private val queue: BlockingQueue<List<AudioMarker>> = LinkedBlockingQueue()
    private val bouncingTimeThresholdInMilliseconds = 2000
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
                if (queue.size > 0 && currentTime - lastBounceTime > bouncingTimeThresholdInMilliseconds) {
                    // Removes irrelevant requests.
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
                    logger.info("Bouncing audio")
                    file?.let { it1 -> reader?.let { it2 -> bounceAudio(it1, it2, mostRecentBounceState) } }
                    logger.info("Finished bouncing audio")
                    lastBounceTime = System.currentTimeMillis()
                }
            }


        }
    }

    fun bounce(file: File, reader: AudioFileReader, activeVerses: List<AudioMarker>) {
        lock.lock()
        try {
            this.file = file
            this.reader = reader
            queue.put(activeVerses)
            lastBounceTime = System.currentTimeMillis()
        } finally {
            lock.unlock()
        }
    }

}