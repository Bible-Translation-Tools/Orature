/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

class Utilities {
    companion object {
        /**
         * Suspends the current thread until the interruption
         * of the lock object occurs or timeout,
         * whichever comes first.
         * @param lockObject resource to receive notification from
         * @param timeout max wait time in milliseconds
         */
        fun waitForListenerExecution(
            lockObject: Object,
            timeout: Int = 5000,
            callback: () -> Unit
        ) {
            synchronized(lockObject) {
                // waits until notify() is called (interrupt) or reaching timeout ... milliseconds
                lockObject.wait(timeout.toLong())
                callback()
            }
        }

        fun notifyListenerExecuted(lockObject: Object) {
            synchronized(lockObject){
                lockObject.notify() // interrupts .wait() on lock object
            }
        }
    }
}