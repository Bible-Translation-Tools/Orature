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