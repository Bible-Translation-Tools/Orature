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
