package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j

import com.install4j.api.launcher.ApplicationLauncher
import com.install4j.api.update.UpdateChecker
import kotlin.concurrent.thread

class UpdateLauncher(private val listener: UpdateProgressListener? = null) : ApplicationLauncher.Callback {

    fun update(onComplete: () -> Unit) {
        ApplicationLauncher.launchApplication("474", null, false, this)
        thread {
            while (!UpdateChecker.isUpdateScheduled()) {
                Thread.sleep(1000)
            }
            onComplete()
        }
    }

    override fun exited(exitValue: Int) {
    }

    override fun prepareShutdown() {
    }

    override fun createProgressListener(): ApplicationLauncher.ProgressListener? {
        return listener
    }
}
