package org.wycliffeassociates.otter.jvm.device.audio

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.sound.sampled.SourceDataLine
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener
import org.wycliffeassociates.otter.common.device.IAudioRecorder

open class AudioConnectionFactory(var line: SourceDataLine) {
    private var player = AudioBufferPlayer(line)
    private val connections = ConcurrentHashMap<Int, IAudioPlayer>()
    private val idgen = AtomicInteger(1)
    private var currentConnection: AudioConnection.State? = null

    fun getRecorder(): IAudioRecorder {
        return AudioRecorder()
    }

    @Synchronized
    fun replaceLine(newLine: SourceDataLine) {
        player.pause()
        line.close()
        line = newLine
        currentConnection?.let {
            load(it)
        }
    }

    fun getPlayer(): IAudioPlayer {
        val id = idgen.getAndIncrement()
        val audioConnection = AudioConnection(id)
        connections[id] = audioConnection
        return audioConnection
    }

    @Synchronized
    protected fun load(request: AudioConnection.State) {
        player.pause()
        currentConnection?.let {
            it.position = player.getLocationInFrames()
            it.durationInFrames = player.getDurationInFrames()
            it.locationInFrames = player.getLocationInFrames()
            it.durationInMs = player.getDurationMs()
            it.durationInFrames = player.getDurationInFrames()
        }
        currentConnection = request
        currentConnection?.let {
            it.durationInFrames = player.getDurationInFrames()
            it.durationInMs = player.getDurationMs()
        }
        line.flush()
        player.stop()
        player = AudioBufferPlayer(line)
        request.listeners.forEach {
            player.addEventListener(it)
        }
        if (request.begin != null && request.end != null) {
            player.loadSection(request.file, request.begin!!, request.end!!)
        } else {
            player.load(request.file)
        }
        player.seek(request.position)
    }

    protected inner class AudioConnection(val id: Int) : IAudioPlayer {

        inner class State(
            val id: Int,
            var file: File = File(""),
            var begin: Int? = null,
            var end: Int? = null,
            var position: Int = 0,
            var durationInFrames: Int = 0,
            var durationInMs: Int = 0,
            var locationInFrames: Int = 0,
            var locationInMs: Int = 0,
            val listeners: MutableList<IAudioPlayerListener> = mutableListOf()
        )

        override val frameStart: Int
            get() = begin
        override val frameEnd: Int
            get() = end

        private var begin = 0
        private var end = 0

        private var state = State(id)

        fun addListeners() {
            currentConnection?.id?.let {
                if (it == id) {
                    state.listeners.forEach {
                        player.addEventListener(it)
                    }
                }
            }
        }

        override fun addEventListener(listener: IAudioPlayerListener) {
            state.listeners.add(listener)
            addListeners()
        }

        override fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit) {
            state.listeners.add(
                object : IAudioPlayerListener {
                    override fun onEvent(event: AudioPlayerEvent) {
                        onEvent(event)
                    }
                }
            )
            addListeners()
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
                    state.position = player.getLocationInFrames()
                    player.pause()
                }
            }
        }

        override fun stop() {
            currentConnection?.id?.let {
                if (it == id) {
                    state.position = player.getLocationInFrames()
                    return player.stop()
                }
            }
        }

        override fun close() {
            currentConnection?.id?.let {
                if (it == id) {
                    player.close()
                    currentConnection = null
                }
            }
            connections.remove(state.id)
        }

        override fun seek(position: Int) {
            this@AudioConnectionFactory.load(state)
            player.seek(position)
            this@AudioConnectionFactory.load(state)
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
            currentConnection?.id?.let {
                if (it == id) {
                    return player.getDurationInFrames()
                }
            }
            return state.durationInFrames
        }

        override fun getDurationMs(): Int {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.getDurationMs()
                }
            }
            return state.durationInMs
        }

        override fun getLocationInFrames(): Int {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.getLocationInFrames()
                }
            }
            return state.locationInFrames
        }

        override fun getLocationMs(): Int {
            currentConnection?.id?.let {
                if (it == id) {
                    return player.getLocationMs()
                }
            }
            return state.locationInMs
        }
    }
}
