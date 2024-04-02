/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.scene.layout.Pane
import javafx.util.Duration
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.popup.NotificationSnackBar
import tornadofx.*

const val NOTIFICATION_DURATION_SEC = 5.0

object SnackbarHandler {

    fun showNotification(message: String, root: Pane) {
        val snackbar = JFXSnackbar(root)
        snackbar.addClass("ethiopic-font")  // include ethiopic font by default
        snackbar.enqueue(
            JFXSnackbar.SnackbarEvent(
                JFXSnackbarLayout(message),
                Duration.seconds(NOTIFICATION_DURATION_SEC)
            )
        )
    }

    fun showNotification(notification: NotificationViewData, container: Pane) {
        val snackbar = JFXSnackbar(container)
        snackbar.addClass("ethiopic-font")  // include ethiopic font by default

        val graphic = NotificationSnackBar(notification).apply {
            setOnDismiss { snackbar.hide() }  /* avoid crashing if close() is invoked before timeout */
            setOnMainAction {
                notification.actionCallback()
                snackbar.hide()
            }
        }
        snackbar.enqueue(
            JFXSnackbar.SnackbarEvent(
                graphic,
                Duration.seconds(NOTIFICATION_DURATION_SEC)
            )
        )
    }
}
