package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel

import java.io.FileNotFoundException
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.AppUpdater
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.UpdateLauncher
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.UpdateProgressListener
import tornadofx.ViewModel

class AppUpdaterViewModel : ViewModel() {

    val showOffline = SimpleBooleanProperty(false)
    val showCheckForUpdate = SimpleBooleanProperty(true)
    val showUpdateAvailable = SimpleBooleanProperty(false)
    val showUpdateDownloading = SimpleBooleanProperty(false)
    val showUpdateCompleted = SimpleBooleanProperty(false)
    val showUpdateScheduled = SimpleBooleanProperty(false)
    val showNoUpdatesAvailable = SimpleBooleanProperty(false)

    val percentCompleteProperty = SimpleIntegerProperty()
    val statusMessageProperty = SimpleStringProperty()
    val detailedMessageProperty = SimpleStringProperty()

    val updateUrlText = SimpleStringProperty()
    val updateSize = SimpleStringProperty()
    val updateVersion = SimpleStringProperty()

    val progressListener = UpdateProgressListener(
        percentCompletedProperty = percentCompleteProperty,
        statusMessageProperty = statusMessageProperty,
        detailMessageProperty = detailedMessageProperty
    )
    val updater = AppUpdater()
    val launcher = UpdateLauncher(progressListener)

    fun checkForUpdates() {
        try {
            showOffline.set(false)
            val result = updater.checkUpdate()
            showCheckForUpdate.set(false)
            if (result.possibleUpdateEntry != null) {
                showUpdateAvailable.set(true)
                updateVersion.set(result.possibleUpdateEntry.newVersion)
                updateSize.set(result.possibleUpdateEntry.fileSizeVerbose)
                updateUrlText.set(result.possibleUpdateEntry.url.toString())
            } else {
                showNoUpdatesAvailable.set(true)
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Exception) {
            showOffline.set(true)
        }
    }

    fun downloadUpdate() {
        showUpdateAvailable.set(false)
        showUpdateDownloading.set(true)
        launcher.update {
            Platform.runLater {
                showUpdateDownloading.set(false)
                showUpdateCompleted.set(true)
            }
        }
    }

    fun updateAndRestart() {
        updater.updateAndRestart()
    }

    fun updateLater() {
        showUpdateCompleted.set(false)
        showUpdateScheduled.set(true)
    }

    fun applyScheduledUpdate() {
        updater.updateAndRestart()
    }
}
