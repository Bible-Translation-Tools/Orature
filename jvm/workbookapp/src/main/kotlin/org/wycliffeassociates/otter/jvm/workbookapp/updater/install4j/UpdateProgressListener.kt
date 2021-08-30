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
        Platform.runLater {
            statusMessageProperty?.set(message)
        }
    }

    override fun detailMessage(message: String?) {
        Platform.runLater {
            detailMessageProperty?.set(message)
        }
    }

    override fun percentCompleted(value: Int) {
        Platform.runLater {
            percentCompletedProperty?.set(value)
        }
    }

    override fun secondaryPercentCompleted(value: Int) {
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
