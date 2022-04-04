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
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.StringProperty
import org.slf4j.LoggerFactory

class UpdateProgressListener(
    private val screenActivatedProperty: StringProperty? = null,
    private val secondaryPercentCompletedProperty: IntegerProperty? = null,
    private val percentCompletedProperty: IntegerProperty? = null,
    private val statusMessageProperty: StringProperty? = null,
    private val detailMessageProperty: StringProperty? = null,
    private val actionStartedProperty: StringProperty? = null,
    private val indeterminateProgressProperty: BooleanProperty? = null
) : ApplicationLauncher.ProgressListener {

    private val logger = LoggerFactory.getLogger(UpdateProgressListener::class.java)

    override fun screenActivated(id: String?) {
        logger.info("Update Screen Activated: $id")
        Platform.runLater {
            screenActivatedProperty?.set(id)
        }
    }

    override fun actionStarted(id: String?) {
        logger.info("Update Action Started: $id")
        Platform.runLater {
            actionStartedProperty?.set(id)
        }
    }

    override fun statusMessage(message: String?) {
        logger.info("status message: $message")
        Platform.runLater {
            statusMessageProperty?.set(message)
        }
    }

    override fun detailMessage(message: String?) {
        logger.info("detail message: $message")
        Platform.runLater {
            detailMessageProperty?.set(message)
        }
    }

    override fun percentCompleted(value: Int) {
        logger.info("percent complete: $value")
        Platform.runLater {
            percentCompletedProperty?.set(value)
        }
    }

    override fun secondaryPercentCompleted(value: Int) {
        logger.info("secondary percent complete: $value")
        Platform.runLater {
            secondaryPercentCompletedProperty?.set(value)
        }
    }

    override fun indeterminateProgress(indeterminateProgress: Boolean) {
        logger.info("Update indeterminate progress: $indeterminateProgress")
        Platform.runLater {
            indeterminateProgressProperty?.set(indeterminateProgress)
        }
    }
}
