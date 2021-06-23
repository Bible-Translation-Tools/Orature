package org.wycliffeassociates.otter.common.device

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.io.File

interface IAudioPlayer {
    val frameStart: Int
    val frameEnd: Int
    fun addEventListener(listener: IAudioPlayerListener)
    fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit)
    fun load(file: File)
    fun loadSection(file: File, frameStart: Int, frameEnd: Int)
    fun getAudioReader(): AudioFileReader?
    fun play()
    fun pause()
    fun stop()
    fun close()
    fun seek(position: Int)
    fun isPlaying(): Boolean
    fun getDurationInFrames(): Int
    fun getDurationMs(): Int
    fun getLocationInFrames(): Int
    fun getLocationMs(): Int
}
