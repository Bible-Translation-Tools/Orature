package org.wycliffeassociates.otter.jvm.workbookapp

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.scene.layout.Pane

object SnackbarHandler {

    private val snackbar = JFXSnackbar()

    fun setWindowRoot(pane: Pane) {
        snackbar.registerSnackbarContainer(pane)
    }

    fun enqueue(event: JFXSnackbar.SnackbarEvent) {
        if (snackbar.popupContainer != null) {
            snackbar.enqueue(event)
        }
    }

    fun enqueue(customSnackbar: JFXSnackbarLayout) {
        if (snackbar.popupContainer != null) {
            snackbar.enqueue(JFXSnackbar.SnackbarEvent(customSnackbar))
        }
    }

    fun enqueue(message: String) {
        if (snackbar.popupContainer != null) {
            snackbar.enqueue(JFXSnackbar.SnackbarEvent(JFXSnackbarLayout(message)))
        }
    }
}

