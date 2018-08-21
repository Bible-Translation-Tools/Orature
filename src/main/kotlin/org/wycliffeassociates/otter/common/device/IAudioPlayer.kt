package org.wycliffeassociates.otter.common.device

import io.reactivex.Completable
import java.io.File

interface IAudioPlayer {

    fun load(file: File): Completable

    fun play()

    fun pause()

    fun stop()

    fun getAbsoluteDurationInFrames(): Int

    fun getAbsoluteDurationMs(): Int

    fun getAbsoluteLocationInFrames(): Int

    fun getAbsoluteLocationMs(): Int
}