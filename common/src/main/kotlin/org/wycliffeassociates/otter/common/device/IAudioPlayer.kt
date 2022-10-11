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
package org.wycliffeassociates.otter.common.device

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.io.Closeable
import java.io.File

/**
 * A player which interacts with the audio.
 */
interface IAudioPlayer : Closeable {
    val frameStart: Int
    val frameEnd: Int
    fun addEventListener(listener: IAudioPlayerListener)
    fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit) {
        addEventListener(WeakAudioListener(object : IAudioPlayerListener {
            override fun onEvent(event: AudioPlayerEvent) {
                onEvent(event)
            }
        }))
    }

    /**
     * Loads the audio file into the player. This will open the file for reading.
     *
     * @param file the target audio file
     */
    fun load(file: File)

    /**
     * Loads part of the audio file into the player. This will open the file for reading.
     *
     * @param file the target audio file
     */
    fun loadSection(file: File, frameStart: Int, frameEnd: Int)

    /**
     * Returns the reader of the audio player.
     */
    fun getAudioReader(): AudioFileReader?

    /**
     * Changes the playback speed of the player.
     */
    fun changeRate(rate: Double)

    /**
     * Starts playing the audio.
     */
    fun play()

    /**
     * Pauses the playback.
     */
    fun pause()

    /**
     * Toggles between play() and pause().
     */
    fun toggle()

    /**
     * Stops and resets the playback.
     */
    fun stop()

    /**
     * Closes the current player. This method must be called
     * after using the player. Otherwise, it may leak the resource as the file
     * was held opened.
     */
    override fun close()

    /**
     * Releases the player and clean up resources. This may include any player
     * that is currently playing.
     */
    fun release()

    /**
     * Jumps to a specific location of the audio.
     *
     * @param position the target location (in frame)
     */
    fun seek(position: Int)

    /**
     * Returns the playing status of the player.
     */
    fun isPlaying(): Boolean

    /**
     * Return the duration of the audio in frames (total frame number).
     */
    fun getDurationInFrames(): Int

    /**
     * Returns the duration of the audio in milliseconds.
     */
    fun getDurationMs(): Int

    /**
     * Returns the current position of the playback thumb in frames.
     */
    fun getLocationInFrames(): Int

    /**
     * Returns the current location of the playback thumb in milliseconds.
     */
    fun getLocationMs(): Int
}
