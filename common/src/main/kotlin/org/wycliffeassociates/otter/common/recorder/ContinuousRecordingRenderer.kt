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
package org.wycliffeassociates.otter.common.recorder

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class ContinuousRecordingRenderer(
    val stream: Observable<ByteArray>,
    val recordingStatus: Observable<Boolean>,
    val width: Int,
    val secondsOnScreen: Int
) : RecordingRenderer(stream, recordingStatus, width, secondsOnScreen) {
    val audioData = ArrayList<Float>(10_000)

    override val dataReceiverDisposable: Disposable = dataReceiver
        .doOnError { e ->
            logger.error("Error in data receiver stream", e)
        }
        .subscribe {
            audioData.add(it)
        }

    override fun clearData() {
        audioData.clear()
    }
}
