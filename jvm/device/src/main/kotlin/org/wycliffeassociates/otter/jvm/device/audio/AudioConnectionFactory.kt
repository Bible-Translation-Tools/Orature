package org.wycliffeassociates.otter.jvm.device.audio

import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.KeyGenerator
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener

open class AudioConnectionFactory {

    private val line = AudioSystem.getSourceDataLine(
        AudioFormat(
            44100F,
            16,
            1,
            true,
            false
        )
    )

    private var player = AudioBufferPlayer(line)
    val connections = ConcurrentHashMap<Int, IAudioPlayer>()
    private val idgen = AtomicInteger(1)
    private var currentConnection: AudioConnection.State? = null


    fun getPlayer(): IAudioPlayer {
        val id = idgen.getAndIncrement()
        val audioConnection = AudioConnection(id)
        connections[id] = audioConnection
        return audioConnection
    }

    @Synchronized
    protected fun load(request: AudioConnection.State) {
        println("here")
        // if (currentConnection?.id ?: 0 != request.id) {
            player.pause()
            currentConnection = request
            line.flush()
            player.stop()
            player = AudioBufferPlayer(line)
            if (request.begin != null && request.end != null) {
                player.loadSection(request.file, request.begin!!, request.end!!)
            } else {
                player.load(request.file)
            }
            player.seek(request.position)
       // }
    }


    private fun getAudioReader(state: AudioConnection.State): AudioFileReader? {
        return null
    }

    private fun play(state: AudioConnection.State) {
    }

    private fun pause(state: AudioConnection.State) {
    }

    private fun stop(state: AudioConnection.State) {
    }

    private fun close(state: AudioConnection.State) {
    }

    private fun seek(position: Int, state: AudioConnection.State) {
    }

    private fun isPlaying(state: AudioConnection.State): Boolean {
        return false
    }

    private fun getDurationInFrames(state: AudioConnection.State): Int {
        return 0
    }

    private fun getDurationMs(state: AudioConnection.State): Int {
        return 0
    }

    private fun getLocationInFrames(state: AudioConnection.State): Int {
        return 0
    }

    private fun getLocationMs(state: AudioConnection.State): Int {
        return 0
    }

    enum class PlayerState{PAUSE, PLAY, STOPPED, COMPLETED}

    protected inner class AudioConnection(val id: Int): IAudioPlayer {

        inner class State(
            val id: Int,
            var file: File = File(""),
            var begin: Int? = null,
            var end: Int? = null,
            var position: Int = 0,
            var status: PlayerState = PlayerState.PAUSE
        )

        override val frameStart: Int
            get() = begin
        override val frameEnd: Int
            get() = end

        private var begin = 0
        private var end = 0

        private var state = State(id)

        override fun addEventListener(listener: IAudioPlayerListener) {
//            TODO("Not yet implemented")
        }

        override fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit) {
//            TODO("Not yet implemented")
        }

        override fun load(file: File) {
            state.file = file
            state.position = 0
            this@AudioConnectionFactory.load(state)
        }

        override fun loadSection(file: File, frameStart: Int, frameEnd: Int) {
            begin = frameStart
            end = frameEnd
            state.file = file
            state.begin = begin
            state.end = end
            state.position = begin
            this@AudioConnectionFactory.load(state)
        }

        override fun getAudioReader(): AudioFileReader? {
            this@AudioConnectionFactory.load(state)
            return player.getAudioReader()
        }

        override fun play() {
            this@AudioConnectionFactory.load(state)
            player.play()
        }

        override fun pause() {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.pause()
                }
            }
        }

        override fun stop() {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.stop()
                }
            }
        }

        override fun close() {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.close()
                }
            }
            currentConnection = null
            connections.remove(state.id)
        }

        override fun seek(position: Int) {
            this@AudioConnectionFactory.load(state)
            player.seek(position)
        }

        override fun isPlaying(): Boolean {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.isPlaying()
                }
            }
            return false
        }

        override fun getDurationInFrames(): Int {
            return 0
        }

        override fun getDurationMs(): Int {
//            this@AudioConnectionFactory.load(state)
//            return player.getDurationMs()
            return 0
        }

        override fun getLocationInFrames(): Int {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.getLocationInFrames()
                }
            }
            return state.position
        }

        override fun getLocationMs(): Int {
//            this@AudioConnectionFactory.load(state)
//            return player.getLocationMs()
            return 0
        }
    }
}


















