package org.wycliffeassociates.otter.common.device

import java.io.File

interface IAudioPlayer {
    fun addEventListener(listener: IAudioPlayerListener)
    fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit)
    fun load(file: File)
    fun loadSection(file: File, frameStart: Int, frameEnd: Int)
    fun play()
    fun pause()
    fun stop()
    fun close()
    fun seek(position: Int)
    fun isPlaying(): Boolean
    fun getAbsoluteDurationInFrames(): Int
    fun getAbsoluteDurationMs(): Int
    fun getAbsoluteLocationInFrames(): Int
    fun getAbsoluteLocationMs(): Int
}
