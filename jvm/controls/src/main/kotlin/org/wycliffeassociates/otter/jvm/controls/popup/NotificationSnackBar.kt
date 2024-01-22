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
package org.wycliffeassociates.otter.jvm.controls.popup

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages
import tornadofx.FX.Companion.primaryStage

private const val SNACK_BAR_TO_SCREEN_RATIO = 9.0/10.0

class NotificationSnackBar(notification: NotificationViewData): HBox() {

    val titleProperty = SimpleStringProperty(notification.title)
    val messageProperty = SimpleStringProperty(notification.message)
    val statusTypeProperty = SimpleObjectProperty<NotificationStatusType>(notification.statusType)
    val actionTextProperty = SimpleStringProperty(notification.actionText)
    val actionIconProperty = SimpleObjectProperty<Ikon>(notification.actionIcon)

    private val mainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(null)
    private val dismissActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("wa-snack-bar")

        maxWidthProperty().bind(primaryStage.widthProperty().multiply(SNACK_BAR_TO_SCREEN_RATIO))

        button {
            addClass("btn", "btn--icon", "btn--borderless", "success-btn-icon")
            graphicProperty().bind(statusTypeProperty.objectBinding {
                this.toggleClass("success-btn-icon", it == NotificationStatusType.SUCCESSFUL)
                this.toggleClass("danger-btn-icon", it != NotificationStatusType.SUCCESSFUL)

                return@objectBinding when (it) {
                    NotificationStatusType.SUCCESSFUL -> {
                        FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
                            addClass("active-icon")
                        }
                    }
                    NotificationStatusType.FAILED, NotificationStatusType.WARNING -> {
                        FontIcon(MaterialDesign.MDI_ALERT).apply {
                            addClass("danger-icon")
                        }
                    }
                    else -> Region()
                }
            })
            isFocusTraversable = false
            isMouseTransparent = true
        }
        vbox {
            addClass("wa-snack-bar__labels")
            label(titleProperty) {
                addClass("h4", "notification-title")
                statusTypeProperty.onChangeAndDoNow {
                    toggleClass("successful-text", it == NotificationStatusType.SUCCESSFUL)
                    toggleClass("danger-text", it != NotificationStatusType.SUCCESSFUL)
                }
            }
            label(messageProperty) {
                addClass("h5", "notification-subtitle")
            }
        }
        region { hgrow = Priority.ALWAYS }
        button {
            addClass("btn", "btn--secondary")
            minWidth = USE_PREF_SIZE
            textProperty().bind(actionTextProperty)
            tooltip { textProperty().bind(actionTextProperty) }
            graphicProperty().bind(actionIconProperty.objectBinding { it ->
                it?.let { FontIcon(it) }
            })
            onActionProperty().bind(mainActionProperty)
            visibleWhen { actionTextProperty.isNotNull }
            managedWhen(visibleProperty())
        }

        button {
            addClass("btn", "btn--icon", "btn--borderless")
            tooltip(messages["dismiss"])
            graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE).apply {
                addClass("dismiss-icon")
            }
            onActionProperty().bind(dismissActionProperty)
        }
    }

    fun setOnMainAction(op: () -> Unit) {
        mainActionProperty.set(EventHandler { op() })
    }

    fun setOnDismiss(op: () -> Unit) {
        dismissActionProperty.set(EventHandler { op() })
    }
}