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
package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j

import com.install4j.api.launcher.ApplicationLauncher
import com.install4j.api.update.UpdateChecker
import kotlin.concurrent.thread
import org.slf4j.LoggerFactory

class UpdateLauncher(private val listener: UpdateProgressListener? = null) : ApplicationLauncher.Callback {

    private val logger = LoggerFactory.getLogger(UpdateLauncher::class.java)

    fun update(onComplete: () -> Unit) {

        logger.info("Launching update application...")
        ApplicationLauncher.launchApplication(
            "474",
            arrayOf("-Dinstall4j.debug=true", "-Dinstall4j.logToStderr=true"),
            false,
            this
        )

        thread {
            while (!UpdateChecker.isUpdateScheduled()) {
                Thread.sleep(1000)
            }
            onComplete()
        }
    }

    override fun exited(exitValue: Int) {
        /* no-op */
    }

    override fun prepareShutdown() {
        /* no-op */
    }

    override fun createProgressListener(): ApplicationLauncher.ProgressListener? {
        logger.info("Creating app update progress listener...")
        return listener
    }
}
